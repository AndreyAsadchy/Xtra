package com.github.exact7.xtra.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.NetworkUtils
import com.github.exact7.xtra.util.TwitchApiHelper

const val FIRST_LAUNCH = "first_launch"

class SplashActivity : AppCompatActivity(), Injectable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(FIRST_LAUNCH, true)
        if (!isFirstLaunch) {
            val user = TwitchApiHelper.getUser(this)
            if (user != null && NetworkUtils.isConnected(this)) { //TODO change to worker and add livedata is userauthorized

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
        startMainActivity()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
