package com.souche.api;

import com.alibaba.fastjson.JSON;
import java.util.Map;

public class SoucheApiException extends Exception {
    private static final long serialVersionUID = 1L;

    private int errStatus;
    private int errCode;
    private String errMessage;

    public SoucheApiException(Throwable cause) {
        super(cause);
        String detailMessage = cause.getMessage();
        try {
            Map errMap = JSON.parseObject(detailMessage);
            if (errMap.get("status") != null) errStatus = (Integer) errMap.get("status");
            if (errMap.get("code") != null) errCode = (Integer) errMap.get("code");
            if (errMap.get("message") != null) errMessage = (String) errMap.get("message");
        } catch (Exception e) {
            // message = detailMessage;
        }
    }

    public SoucheApiException(int errCode, String errMessage) {
        super(errCode + ": " + errMessage);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public int getErrStatus() {
        return errStatus;
    }

    public void setErrStatus(int errStatus) {
        this.errStatus = errStatus;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
