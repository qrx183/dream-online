package com.xuecheng.exception;

public class XcPlusException extends RuntimeException{
    
    private String errMessage;

    public XcPlusException() {
        super();
    }

    public XcPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public static void cast(CommonError commonError) {
        throw new XcPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new XcPlusException(errMessage);
    }
}

