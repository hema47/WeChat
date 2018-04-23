package com.example.hp.chatapp.Model;

/**
 * Created by HP on 22-04-2018.
 */

public class FriendRequest {
    String request_type;

    public FriendRequest() {

    }

    public FriendRequest(String request_type) {
        this.request_type = request_type;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
