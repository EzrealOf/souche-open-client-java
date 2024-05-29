package com.souche.api.util;

public class StringUtils {

    /**
     * 检查字符串是否为空
     * @param {String} value 待检查字符串
     * @return {boolean}
     * */
    public static boolean isEmpty(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(value.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
