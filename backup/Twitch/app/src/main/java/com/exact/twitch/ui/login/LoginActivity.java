package com.exact.twitch.ui.login;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.exact.twitch.R;
import com.exact.twitch.model.id.ValidationResponse;
import com.exact.twitch.repository.AuthRepository;
import com.exact.twitch.util.TwitchApiHelper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Inject
    AuthRepository repository;

    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        webView = findViewById(R.id.web_view);
        Button btnSignIn = findViewById(R.id.activity_login_btn_sign_in);
        Button btnSkip = findViewById(R.id.activity_login_btn_skip);
        btnSignIn.setOnClickListener(v -> webView.setVisibility(View.VISIBLE));
        btnSkip.setOnClickListener(v -> onBackPressed()); //TODO maybe change
        webView.getSettings().setJavaScriptEnabled(true);
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        webView.setWebViewClient(new WebViewClient() {

            private Pattern pattern = Pattern.compile("token=(.+)&");

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    String token = matcher.group(1);
                    view.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = getSharedPreferences("auth", MODE_PRIVATE).edit();
                    repository.validate("OAuth " + token, new Callback<ValidationResponse>() {
                        @Override
                        public void onResponse(Call<ValidationResponse> call, Response<ValidationResponse> response) {
                            if (response.isSuccessful()) {
                                ValidationResponse body = response.body();
                                editor.putString("token", token);
                                editor.putString("username", body.getLogin());
                                editor.putString("user_id", body.getUserId());
                                editor.apply();
                                onBackPressed();
                            }
                        }

                        @Override
                        public void onFailure(Call<ValidationResponse> call, Throwable t) {

                        }
                    });
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        if (token != null) {
            repository.revoke(token, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    prefs.edit().clear().commit();
                    webView.loadUrl("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=" + TwitchApiHelper.INSTANCE.getClientId()+ "&redirect_uri=http://localhost&scope=chat_login user_follows_edit user_read user_subscriptions");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } else {
            webView.loadUrl("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=" + TwitchApiHelper.INSTANCE.getClientId()+ "&redirect_uri=http://localhost&scope=chat_login user_follows_edit user_read user_subscriptions");
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
