package com.xuecheng.exception;

public class RestErrorResponse {
    private String restErrorMessage;

    public RestErrorResponse(String message){
        this.restErrorMessage = message;
    }

    public String getRestErrorMessage() {
        return restErrorMessage;
    }

    public void setRestErrorMessage(String restErrorMessage) {
        this.restErrorMessage = restErrorMessage;
    }
}
