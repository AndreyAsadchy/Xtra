package com.github.exact7.xtra.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.repository.AuthRepository
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.NetworkUtils
import com.github.exact7.xtra.util.TwitchApiHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SplashActivity : AppCompatActivity(), Injectable {

    private companion object {
        const val FIRST_LAUNCH = "first_launch"
    }

    @Inject
    lateinit var authRepository: AuthRepository
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(FIRST_LAUNCH, true)
        if (!isFirstLaunch) {
            val user = TwitchApiHelper.getUser(this)
            if (user != null && NetworkUtils.isConnected(this)) {
                authRepository.validate(user.token)
                        .subscribe({
                            startMainActivity(user)
                        }, {
                            getSharedPreferences(C.AUTH_PREFS, Context.MODE_PRIVATE).edit { clear() }
                            Toast.makeText(this, getString(R.string.token_expired), Toast.LENGTH_LONG).show()
                            startActivityForResult(Intent(this, LoginActivity::class.java), 1)
                        })
                        .addTo(compositeDisposable)
            } else {
                startMainActivity()
            }
        } else {
            prefs.edit { putBoolean(FIRST_LAUNCH, false) }
            startActivityForResult(Intent(this, LoginActivity::class.java).apply { putExtra(FIRST_LAUNCH, true) }, 1)
        }
    }

    /**
     * Result of LoginActivity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startMainActivity(data?.getParcelableExtra(C.USER))
    }

    private fun startMainActivity(user: User? = null) {
        val intent = Intent(this, MainActivity::class.java)
        user?.let { intent.putExtra(C.USER, user) }
        startActivity(intent)
        finish()
    }
}
