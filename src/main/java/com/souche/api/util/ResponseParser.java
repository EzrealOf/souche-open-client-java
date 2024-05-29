package com.souche.api.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.souche.api.SoucheApiException;
import com.souche.api.SoucheResponse;

public class ResponseParser {

    public static SoucheResponse parse(String responseStr) throws SoucheApiException {
        try {
            return JSON.parseObject(responseStr, SoucheResponse.class);
        } catch (JSONException e) {
            throw new SoucheApiException(e);
        }
    }
}
