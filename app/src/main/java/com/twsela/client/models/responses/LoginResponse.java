
package com.twsela.client.models.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.twsela.client.models.entities.User;

public class LoginResponse extends ServerResponse {
    @SerializedName("content")
    @Expose
    private User content;

    public User getContent() {
        return content;
    }

    public void setContent(User content) {
        this.content = content;
    }
}
