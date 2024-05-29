package com.souche.api.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.souche.api.SoucheApiException;
import com.souche.api.SoucheConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class SoucheSignature {

    public static String sign(Map<String, String> reqMap, String appSecret) throws SoucheApiException {

        try {
            // 排序拼接字符串
            String sortedJoinedStr = mapJoin(getSortedMap(reqMap));

            // base64
            String base64Str = new String(Base64.encodeBase64(sortedJoinedStr.getBytes(SoucheConstants.CHARSET_UTF8)));

            // 拼接上 appSecret:
            String strToEncrypt = appSecret + ":" + base64Str;

            // sha1 加密
            return DigestUtils.sha1Hex(strToEncrypt);
        } catch (UnsupportedEncodingException e) {
            throw new SoucheApiException(e);
        }

    }

    private static Map<String, String> getSortedMap(Map<String, String> reqMap) {
        return new TreeMap<String, String>(reqMap);
    }

    private static String mapJoin(Map<String, String> sortedParams) {
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList<String>(sortedParams.keySet());
        Collections.sort(keys);
        int index = 0;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = sortedParams.get(key);
            content.append((index == 0 ? "" : "&") + key + "=" + value);
            index++;
        }
        return content.toString();
    }

    public static boolean check(String reqJsonString, String appSecret) throws SoucheApiException {
        try {
            Map<String, Object> reqMap = JSON.parseObject(reqJsonString);
            Map<String, String> reqStringMap = new HashMap<String, String>();
            for (Map.Entry<String, Object> entry : reqMap.entrySet()) {
                if(entry.getValue() instanceof String){
                    reqStringMap.put(entry.getKey(), (String) entry.getValue());
                } else {
                    reqStringMap.put(entry.getKey(), entry.getValue().toString());
                }
            }
            if (reqStringMap.get("appKey") == null || reqStringMap.get("data") == null || reqStringMap.get("timestamp") == null || reqStringMap.get("sign") == null) {
                return false;
            }
            String signature = reqStringMap.get("sign");
            reqStringMap.remove("sign");
            return signature.equals(sign(reqStringMap, appSecret));
        } catch (JSONException e) {
            return false;
        }
    }
}
