package com.twsela.client.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.twsela.client.Const;
import com.twsela.client.R;
import com.twsela.client.activities.TripActivity;
import com.twsela.client.activities.TripDetailsActivity;
import com.twsela.client.controllers.ActiveUserController;
import com.twsela.client.models.enums.NotificationKey;
import com.twsela.client.models.enums.TripStatus;
import com.twsela.client.models.events.TripStatusChanged;
import com.twsela.client.models.payloads.TripPayload;
import com.twsela.client.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Shamyyoun on 11/29/16.
 */
public class FCMHandlerService extends FirebaseMessagingService {
    private static final String TAG = "FCMHandler";
    private ActiveUserController activeUserController;

    @Override
    public void onCreate() {
        super.onCreate();
        activeUserController = new ActiveUserController(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // log the details
        logDetails(remoteMessage);

        // check active user
        ActiveUserController activeUserController = new ActiveUserController(this);
        if (!activeUserController.hasLoggedInUser()) {
            return;
        }

        // check data map
        Map data = remoteMessage.getData();
        if (data == null) {
            return;
        }

        // prepare key and content
        String key = null;
        String contentStr = null;
        try {
            String key1Str = remoteMessage.getData().get("key1");
            JSONObject key1Object = new JSONObject(key1Str);
            JSONObject dataObject = key1Object.getJSONObject("data");
            key = dataObject.getString("key");
            contentStr = dataObject.getJSONObject("content").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (NotificationKey.DRIVER_ACCEPTED_TRIP.getValue().equals(key)) {
            handleDriverAcceptedNotification(contentStr);
        } else if (NotificationKey.DRIVER_ARRIVED.getValue().equals(key)) {
            handleDriverArrivedNotification(contentStr);
        } else if (NotificationKey.DRIVER_STARTED_TRIP.getValue().equals(key)) {
            handleTripStartedNotification(contentStr);
        } else if (NotificationKey.DRIVER_ENDED_TRIP.getValue().equals(key)) {
            handleTripEndedNotification(contentStr);
        } else if (NotificationKey.DRIVER_CANCELLED_TRIP.getValue().equals(key)) {
            handleTripCancelledNotification(contentStr);
        }
    }

    private void handleDriverAcceptedNotification(String contentStr) {
        // parse the trip payload object
        Gson gson = new Gson();
        TripPayload payload = gson.fromJson(contentStr, TripPayload.class);

        // validate the payload
        if (payload == null) {
            return;
        }

        // update last trip status
        activeUserController.updateActiveTripStatus(payload.getTripId(), TripStatus.ACCEPTED);

        // show notification
        showNotification(Const.NOTI_TRIP_CHANGED, getString(R.string.your_driver_in_his_way_to_pickup));

        // open trip activity
        Intent intent = new Intent(this, TripActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Const.KEY_ID, payload.getTripId());
        intent.putExtra(Const.KEY_STATUS, TripStatus.ACCEPTED.getValue());
        startActivity(intent);

        // post event
        TripStatusChanged event = new TripStatusChanged();
        event.setStatus(TripStatus.ACCEPTED);
        EventBus.getDefault().post(event);
    }

    private void handleDriverArrivedNotification(String contentStr) {
        // parse the trip payload object
        Gson gson = new Gson();
        TripPayload payload = gson.fromJson(contentStr, TripPayload.class);

        // validate the payload
        if (payload == null) {
            return;
        }

        // update last trip status
        activeUserController.updateActiveTripStatus(payload.getTripId(), TripStatus.DRIVER_ARRIVED);

        // show notification
        showNotification(Const.NOTI_TRIP_CHANGED, getString(R.string.your_driver_has_arrived_to_pickup));

        // post event
        TripStatusChanged event = new TripStatusChanged();
        event.setStatus(TripStatus.DRIVER_ARRIVED);
        EventBus.getDefault().post(event);
    }

    private void handleTripStartedNotification(String contentStr) {
        // parse the trip payload object
        Gson gson = new Gson();
        TripPayload payload = gson.fromJson(contentStr, TripPayload.class);

        // validate the payload
        if (payload == null) {
            return;
        }

        // update last trip status
        activeUserController.updateActiveTripStatus(payload.getTripId(), TripStatus.STARTED);

        // show notification
        showNotification(Const.NOTI_TRIP_CHANGED, getString(R.string.your_trip_has_started));

        // post event
        TripStatusChanged event = new TripStatusChanged();
        event.setStatus(TripStatus.STARTED);
        EventBus.getDefault().post(event);
    }

    private void handleTripEndedNotification(String contentStr) {
        // parse the trip payload object
        Gson gson = new Gson();
        TripPayload payload = gson.fromJson(contentStr, TripPayload.class);

        // validate the payload
        if (payload == null) {
            return;
        }

        // update last trip status
        activeUserController.removeActiveTrip();

        // show notification
        showNotification(Const.NOTI_TRIP_CHANGED, getString(R.string.you_have_arrived));

        // open trip details activity
        Intent intent = new Intent(this, TripDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Const.KEY_ID, payload.getTripId());
        startActivity(intent);

        // post event
        TripStatusChanged event = new TripStatusChanged();
        event.setStatus(TripStatus.ENDED);
        EventBus.getDefault().post(event);
    }

    private void handleTripCancelledNotification(String contentStr) {
        // update last trip status
        activeUserController.removeActiveTrip();

        // show notification
        showNotification(Const.NOTI_TRIP_CHANGED, getString(R.string.sorry_driver_cancelled_your_trip));

        // post event
        TripStatusChanged event = new TripStatusChanged();
        event.setStatus(TripStatus.CANCELLED);
        EventBus.getDefault().post(event);
    }

    private void showNotification(int notificationId, String message) {
        // create the notification
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.twsela))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);
        builder.getNotification().flags = Notification.FLAG_AUTO_CANCEL;

        // show the notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }

    private void logDetails(RemoteMessage remoteMessage) {
        if (!Utils.DEBUGGABLE) {
            return;
        }

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
        } else {
            Log.e(TAG, "Message doesn't contain data payload.");
        }

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        } else {
            Log.e(TAG, "Message doesn't contain Notification Body.");
        }
    }
}