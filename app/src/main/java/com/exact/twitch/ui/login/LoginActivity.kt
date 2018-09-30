package com.exact.twitch.ui.login

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.exact.twitch.R
import com.exact.twitch.repository.AuthRepository
import com.exact.twitch.util.C
import com.exact.twitch.util.TwitchApiHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AuthRepository
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        CookieManager.getInstance().removeAllCookie()
        webView.settings.javaScriptEnabled = true
        val prefs = getSharedPreferences(C.AUTH_PREFS, MODE_PRIVATE)
        webView.webViewClient = object : WebViewClient() {

            private val pattern = Pattern.compile("token=(.+?)(?=&)")

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val matcher = pattern.matcher(url)
                if (matcher.find()) {
                    view.visibility = View.GONE
                    val token = matcher.group(1)
                    repository.validate("OAuth $token")
                            .subscribe { response ->
                                prefs.edit()
                                        .putString(C.TOKEN, token)
                                        .putString(C.USERNAME, response.login)
                                        .putString("user_id", response.userId)
                                        .apply()
//                                findNavController(R.id.fragmentContainer).navigate(R.id.fragment_follow)
                            }
                            .addTo(compositeDisposable)
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        prefs.getString(C.TOKEN, null)?.let {
            repository.revoke(it)
                    .subscribe { _ -> prefs.edit().clear().apply() }
                    .addTo(compositeDisposable)
        }
        webView.loadUrl("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=${TwitchApiHelper.clientId}&redirect_uri=http://localhost&scope=chat_login user_follows_edit user_read user_subscriptions")
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> if (webView.canGoBack()) {
                    webView.goBack()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}