package com.twsela.client.controllers;

import android.content.Context;

import com.twsela.client.Const;
import com.twsela.client.models.entities.User;
import com.twsela.client.models.enums.TripStatus;
import com.twsela.client.utils.SharedPrefs;

/**
 * Created by Shamyyoun on 8/27/16.
 */
public class ActiveUserController {
    private static User user;
    private Context context;
    private SharedPrefs<User> userPrefs;

    public ActiveUserController(Context context) {
        this.context = context;
        userPrefs = new SharedPrefs(context, User.class);
    }

    public void save() {
        userPrefs.save(user, Const.SP_USER);
    }

    public User getUser() {
        if (user == null) {
            user = userPrefs.load(Const.SP_USER);
        }

        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean hasLoggedInUser() {
        return getUser() != null;
    }

    public void logout() {
        userPrefs.remove(Const.SP_USER);
        setUser(null);
    }

    public void updateLastTripStatus(TripStatus status) {
        if (status == null) {
            user.setLastTripStatus(null);
        } else {
            user.setLastTripStatus(status.getValue());
        }
        save();
    }
}
