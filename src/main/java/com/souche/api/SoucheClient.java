package com.souche.api;

import com.alibaba.fastjson.JSON;
import com.souche.api.util.ResponseParser;
import com.souche.api.util.SoucheSignature;
import com.souche.api.util.WebUtils;

import java.util.HashMap;
import java.util.Map;

public class SoucheClient {

    private String serverUrl;
    private String appKey;
    private String appSecret;

    public SoucheClient(String serverUrl, String appKey, String appSecret) {
        this.serverUrl = serverUrl;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    /**
     * 执行请求操作
     *
     * @param request 请求类
     * @return  响应类
     * */
    public SoucheResponse execute(SoucheRequest request) throws SoucheApiException {
        String responseStr = _execute(request);
        return ResponseParser.parse(responseStr);
    }

    /**
     * 执行请求操作
     *
     * @param requestMap 请求 Map
     * @return 响应字符串
     * */
    public String execute(Map<String, Object> requestMap) throws SoucheApiException {

        Map<String, String> reqMap = getRequestMapWithSign(requestMap);

        return doPost(JSON.toJSONString(reqMap), (Map<String, String>) requestMap.get(SoucheConstants.HEADERS));
    }

    public String _execute(SoucheRequest request) throws SoucheApiException {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put(SoucheConstants.API_KEY, request.getApi());
        requestMap.put(SoucheConstants.DATA_CONTENT_KEY, request.getData());
        requestMap.put(SoucheConstants.HEADERS, request.getHeader());

        return execute(requestMap);
    }

    private Map<String, String> getRequestMapWithSign(Map<String, Object> reqwithoutSignMap) throws SoucheApiException {
        Map<String, String> reqMap = new HashMap<String, String>();

        // data
        reqMap.put(SoucheConstants.DATA_CONTENT_KEY, JSON.toJSONString(reqwithoutSignMap.get(SoucheConstants.DATA_CONTENT_KEY)));

        // api appKey
        reqMap.put(SoucheConstants.API_KEY, (String) reqwithoutSignMap.get(SoucheConstants.API_KEY));
        reqMap.put(SoucheConstants.APPKEY_KEY, this.appKey);

        // timestamp
        Long timestamp = System.currentTimeMillis() / 1000;
        reqMap.put(SoucheConstants.TIMESTAMP_KEY, timestamp.toString());

        // sign
        String sign = SoucheSignature.sign(reqMap, appSecret);
        reqMap.put(SoucheConstants.SIGN_KEY, sign);

        return reqMap;
    }

    private String doPost(String postBodyJsonStr, Map<String, String> header) throws SoucheApiException {
        String rsp;
        try {
            rsp = WebUtils.doPost(serverUrl, SoucheConstants.CONTENT_TYPE_JSON, postBodyJsonStr.getBytes(SoucheConstants.CHARSET_UTF8),
                    SoucheConstants.CONNECT_TIMEOUT, SoucheConstants.READ_TIMEOUT, header);
        } catch (Exception e) {
            throw new SoucheApiException(e);
        }

        return rsp;
    }
}
