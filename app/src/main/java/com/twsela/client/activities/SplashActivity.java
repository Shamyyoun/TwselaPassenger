package com.twsela.client.activities;

import android.content.Intent;
import android.os.Bundle;

import com.twsela.client.R;
import com.twsela.client.controllers.ActiveUserController;
import com.twsela.client.utils.PlayServicesUtils;
import com.twsela.client.utils.Utils;

public class SplashActivity extends ParentActivity {
    private ActiveUserController activeUserController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // check google play services
        if (!PlayServicesUtils.isPlayServicesAvailable(this)) {
            // show msg and finish
            Utils.showLongToast(this, R.string.install_google_play_services);
            finish();
            return;
        }

        // create the controller
        activeUserController = new ActiveUserController(this);

        // check logged in user to goto suitable activity
        Intent intent = new Intent();
        if (activeUserController.hasLoggedInUser()) {
            intent.setClass(this, MainActivity.class);
        } else {
            intent.setClass(this, LoginActivity.class);
        }

        // open the activity
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}