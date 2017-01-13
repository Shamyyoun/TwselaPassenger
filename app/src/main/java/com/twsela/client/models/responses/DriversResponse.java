
package com.twsela.client.models.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.twsela.client.models.entities.Driver;

import java.util.List;

public class DriversResponse extends ServerResponse {
    @SerializedName("content")
    @Expose
    private List<Driver> content = null;

    public List<Driver> getContent() {
        return content;
    }

    public void setContent(List<Driver> content) {
        this.content = content;
    }
}
