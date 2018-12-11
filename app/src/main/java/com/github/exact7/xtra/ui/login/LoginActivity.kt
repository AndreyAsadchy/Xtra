package com.github.exact7.xtra.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.repository.AuthRepository
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.TwitchApiHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), Injectable {

    @Inject
    lateinit var repository: AuthRepository
    private val compositeDisposable = CompositeDisposable()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        prefs = getSharedPreferences(C.AUTH_PREFS, MODE_PRIVATE)
        val token = prefs.getString(C.TOKEN, null)
        if (token == null) {
            if (intent.getBooleanExtra("first_launch", false)) {
                welcomeContainer.visibility = View.VISIBLE
                login.setOnClickListener { initWebView() }
                skip.setOnClickListener { finish() }
            } else {
                initWebView()
            }
        } else {
            initWebView()
            prefs.edit { clear() }
            repository.revoke(token)
                    .subscribe { _ -> prefs.edit { clear() } }
                    .addTo(compositeDisposable)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.visibility = View.VISIBLE
        CookieManager.getInstance().removeAllCookies(null)
        with(webView) {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {

                private val pattern = Pattern.compile("token=(.+?)(?=&)")

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val matcher = pattern.matcher(url)
                    if (matcher.find()) {
                        webView.visibility = View.GONE
                        welcomeContainer.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        val token = matcher.group(1)
                        repository.validate(token)
                                .subscribe { response ->
                                    val user = User(response.userId, response.username, token)
                                    prefs.edit {
                                        putString(C.TOKEN, token)
                                        putString(C.USERNAME, response.username)
                                        putString("user_id", response.userId)
                                    }
                                    val resultIntent = Intent().apply { putExtra(C.USER, user) }
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                }
                                .addTo(compositeDisposable)
                    }
                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    view?.apply {
                        val html ="<html><body><div align=\"center\" >No internet connection</div></body>"
                        loadUrl("about:blank")
                        loadDataWithBaseURL(null , html, "text/html", "UTF-8", null)
                    }
                }
            }
            loadUrl("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=${TwitchApiHelper.clientId}&redirect_uri=http://localhost&scope=chat_login user_follows_edit user_subscriptions user_read")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}