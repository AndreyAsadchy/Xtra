package com.github.exact7.xtra.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.C.FIRST_LAUNCH


class SplashActivity : AppCompatActivity(), Injectable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FIRST_LAUNCH, true)) {
            startMainActivity()
        } else {
            startActivityForResult(Intent(this, LoginActivity::class.java).apply { putExtra(FIRST_LAUNCH, true) }, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startMainActivity()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
