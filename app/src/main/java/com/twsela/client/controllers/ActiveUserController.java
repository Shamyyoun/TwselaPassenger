package com.twsela.client.controllers;

import android.content.Context;

import com.twsela.client.Const;
import com.twsela.client.models.entities.Trip;
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

    public void updateActiveTripStatus(String id, TripStatus status) {
        // create new one if required
        Trip activeTrip = user.getActiveTrip();
        if (activeTrip == null) {
            activeTrip = new Trip();
        }

        // set values
        activeTrip.setId(id);
        activeTrip.setStatus(status.getValue());

        user.setActiveTrip(activeTrip);
        save();
    }

    public void removeActiveTrip() {
        user.setActiveTrip(null);
        save();
    }

    public Trip getActiveTrip() {
        if (user != null) {
            return user.getActiveTrip();
        } else {
            return null;
        }
    }
}
