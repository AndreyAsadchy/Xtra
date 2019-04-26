package com.github.exact7.xtra.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.LoggedIn
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.repository.AuthRepository
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.Prefs
import com.github.exact7.xtra.util.TwitchApiHelper
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.visible
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), Injectable {

    @Inject
    lateinit var repository: AuthRepository
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(if (Prefs.get(this).getBoolean(C.THEME, true)) R.style.DarkTheme else R.style.LightTheme)
        try {
            setContentView(R.layout.activity_login)
        } catch (e: Exception) {
            Crashlytics.logException(e)
            if (e.message?.contains("WebView") == true) {
                Crashlytics.log("LoginActivity.onCreate: WebView not found. Message: ${e.message}")
                Toast.makeText(this, "Error loading web browser. This might be because it's being updated right now. Please try again in a few minutes.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
        try { //TODO remove after updated to 1.2.0
            val oldPrefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
            if (oldPrefs.all.isNotEmpty()) {
                with(oldPrefs) {
                    if (getString(C.USER_ID, null) != null) {
                        Prefs.setUser(this@LoginActivity, LoggedIn(getString(C.USER_ID, null)!!, getString(C.USERNAME, null)!!, getString(C.TOKEN, null)!!))
                    }
                }
                oldPrefs.edit { clear() }
            }
        } catch (e: Exception) {

        }
        val user = Prefs.getUser(this)
        if (user is NotLoggedIn) {
            if (intent.getBooleanExtra(C.FIRST_LAUNCH, false)) {
                welcomeContainer.visible()
                login.setOnClickListener { initWebView() }
                skip.setOnClickListener { finish() }
            } else {
                initWebView()
            }
        } else {
            repository.deleteAllEmotes()
            TwitchApiHelper.validated = false
            initWebView()
            if (!intent.getBooleanExtra("expired", false)) {
                repository.revoke(user.token)
                        .subscribe { _ -> Prefs.setUser(this, null) }
                        .addTo(compositeDisposable)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.visible()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
        } else {
            CookieManager.getInstance().removeAllCookie()
        }
        with(webView) {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {

                private val pattern = Pattern.compile("token=(.+?)(?=&)")

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val matcher = pattern.matcher(url)
                    if (matcher.find()) {
                        webView.gone()
                        welcomeContainer.gone()
                        progressBar.visible()
                        val token = matcher.group(1)
                        repository.validate(token)
                                .subscribe { response ->
                                    TwitchApiHelper.validated = true
                                    Prefs.setUser(this@LoginActivity, LoggedIn(response.userId, response.username, token))
                                    setResult(RESULT_OK)
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
            loadUrl("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=${TwitchApiHelper.getClientId()}&redirect_uri=http://localhost&scope=chat_login user_follows_edit user_subscriptions user_read")
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