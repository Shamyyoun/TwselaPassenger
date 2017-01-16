package com.twsela.client.activities;

import android.content.Intent;
import android.os.Bundle;

import com.twsela.client.Const;
import com.twsela.client.R;
import com.twsela.client.controllers.ActiveUserController;
import com.twsela.client.models.entities.Trip;
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
            Utils.showLongToast(this, R.string.install_or_update_google_play_services);
            finish();
            return;
        }

        // create the controller
        activeUserController = new ActiveUserController(this);

        // check logged in user
        if (activeUserController.hasLoggedInUser()) {
            // open main activity
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);

            // check active trip
            Trip activeTrip = activeUserController.getActiveTrip();
            if (activeTrip != null) {
                // open trip details activity
                Intent tripIntent = new Intent(this, TripActivity.class);
                tripIntent.putExtra(Const.KEY_ID, activeTrip.getId());
                tripIntent.putExtra(Const.KEY_STATUS, activeTrip.getStatus());

                startActivity(tripIntent);
            }

        } else {
            // open login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        // finish
        finish();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}