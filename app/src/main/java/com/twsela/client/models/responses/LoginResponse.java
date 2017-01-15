
package com.twsela.client.models.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.twsela.client.models.entities.Passenger;

public class LoginResponse extends ServerResponse {
    @SerializedName("content")
    @Expose
    private Passenger content;

    public Passenger getContent() {
        return content;
    }

    public void setContent(Passenger content) {
        this.content = content;
    }
}
