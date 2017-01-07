package com.twsela.client.connection;

public interface ConnectionListener<T> {
    void onSuccess(T response, int statusCode, String tag);

    void onFail(Exception ex, int statusCode, String tag);
}
