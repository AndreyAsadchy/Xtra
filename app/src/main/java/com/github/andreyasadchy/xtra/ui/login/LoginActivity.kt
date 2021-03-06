package com.github.andreyasadchy.xtra.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.ui.Utils
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.applyTheme
import com.github.andreyasadchy.xtra.util.convertDpToPixels
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.installPlayServicesIfNeeded
import com.github.andreyasadchy.xtra.util.shortToast
import com.github.andreyasadchy.xtra.util.toast
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), Injectable {

    @Inject
    lateinit var repository: AuthRepository

    private val authUrl = "https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=${TwitchApiHelper.CLIENT_ID}&redirect_uri=https://localhost&scope=chat_login user_follows_edit user_subscriptions user_read"
    private val tokenPattern = Pattern.compile("token=(.+?)(?=&)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_login)
        val user = User.get(this)
        if (user is NotLoggedIn) {
            if (intent.getBooleanExtra(C.FIRST_LAUNCH, false)) {
                installPlayServicesIfNeeded()
                welcomeContainer.visible()
                login.setOnClickListener { initWebView() }
                skip.setOnClickListener { finish() }
            } else {
                initWebView()
            }
        } else {
            TwitchApiHelper.checkedValidation = false
            User.set(this, null)
            initWebView()
            repository.deleteAllEmotes()
            GlobalScope.launch {
                try {
                    repository.revoke(user.token)
                } catch (e: Exception) {

                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webViewContainer.visible()
        welcomeContainer.gone()
        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(this@LoginActivity)
            setNavigationOnClickListener { finish() }
        }
        havingTrouble.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.login_problem_solution))
                    .setPositiveButton(R.string.log_in) { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW, authUrl.toUri())
                        if (intent.resolveActivity(packageManager) != null) {
                            webView.reload()
                            startActivity(intent)
                        } else {
                            toast(R.string.no_browser_found)
                        }
                    }
                    .setNeutralButton(R.string.to_enter_url) { _, _ ->
                        val editText = EditText(this).apply {
                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                val margin = convertDpToPixels(10f)
                                setMargins(margin, 0, margin, 0)
                            }
                        }
                        val dialog = AlertDialog.Builder(this)
                                .setTitle(R.string.enter_url)
                                .setView(editText)
                                .setPositiveButton(R.string.log_in) { _, _ ->
                                    val text = editText.text
                                    if (text.isNotEmpty()) {
                                        if (!loginIfValidUrl(text.toString())) {
                                            shortToast(R.string.invalid_url)
                                        }
                                    }
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .show()
                        dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        }
        clearCookies()
        with(webView) {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    loginIfValidUrl(url)
                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                    val errorMessage = if (errorCode == -11) {
                        getString(R.string.browser_workaround)
                    } else {
                        getString(R.string.error, "$errorCode $description")
                    }
                    val html = "<html><body><div align=\"center\">$errorMessage</div></body>"
                    loadUrl("about:blank")
                    loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }
            }
            loadUrl(authUrl)
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

    private fun loginIfValidUrl(url: String): Boolean {
        val matcher = tokenPattern.matcher(url)
        return if (matcher.find()) {
            webViewContainer.gone()
            welcomeContainer.gone()
            progressBar.visible()
            val token = matcher.group(1)!!
            lifecycleScope.launch {
                try {
                    val response = repository.validate(token)
                    if (response != null) {
                        TwitchApiHelper.checkedValidation = true
                        User.set(this@LoginActivity, LoggedIn(response.userId, response.username, token))
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        throw IOException()
                    }
                } catch (e: Exception) {
                    clearCookies()
                    webViewContainer.visible()
                    progressBar.gone()
                    webView.loadUrl(authUrl)
                    toast(R.string.connection_error)
                }
            }
            true
        } else {
            false
        }
    }

    private fun clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
        } else {
            CookieManager.getInstance().removeAllCookie()
        }
    }
}