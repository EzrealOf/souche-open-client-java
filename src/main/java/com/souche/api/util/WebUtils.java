package com.souche.api.util;

import com.souche.api.SoucheConstants;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebUtils {

    private static final String DEFAULT_CHARSET = SoucheConstants.CHARSET_UTF8;

    private static final String METHOD_POST = "POST";

    private static SSLContext ctx = null;

    private static SSLSocketFactory socketFactory = null;

    private static HostnameVerifier verifier = null;

    private static class DefaultTrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    }

    static {
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());

            ctx.getClientSessionContext().setSessionTimeout(15);
            ctx.getClientSessionContext().setSessionCacheSize(1000);

            socketFactory = ctx.getSocketFactory();
        } catch (Exception e) {

        }

        verifier = new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslSession) {
                return false;
            }
        };
    }

    /**
     * http post 请求
     *
     * @param url 请求地址
     * @param contentType MIME类型 "application/json;charset=utf-8"
     * @param content 请求体字节数组
     * @param connectTimeout 连接超时时间
     * @param readTimeout 请求超时时间
     *
     * @return 响应字符串
     * @throws IOException
     * */
    public static String doPost(String url, String contentType, byte[] content, int connectTimeout, int readTimeout, Map<String, String> header) throws IOException {

        HttpURLConnection connection = null;
        OutputStream out = null;
        String rsp = null;
        try {
            try {
                URL Url = null;
                if (url.indexOf("https") == 0) {
                    Url = new URL(null, url, new sun.net.www.protocol.https.Handler());
                } else {
                    Url = new URL(null, url, new sun.net.www.protocol.http.Handler());
                }
                connection = getConnection(Url, METHOD_POST, contentType);

                if (header != null) {
                    setHeaders(connection, header);
                }
                connection.setConnectTimeout(connectTimeout);
                connection.setReadTimeout(readTimeout);
            } catch (IOException e) {
                throw e;
            }
            try {
                out = connection.getOutputStream();
                out.write(content);
                rsp = getResponseAsString(connection);
            } catch (IOException e) {
                throw e;
            }
        } finally {
            if (out != null) {
                out.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return rsp;
    }

    private static HttpURLConnection getConnection(URL url, String method, String contentType) throws IOException {
        HttpURLConnection connection = null;
        if ("https".equals(url.getProtocol())) {
            HttpsURLConnection connectionHttps = (HttpsURLConnection) url.openConnection();
            connectionHttps.setSSLSocketFactory(socketFactory);
            connectionHttps.setHostnameVerifier(verifier);
            connection = connectionHttps;
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }

        connection.setRequestMethod(method);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", contentType);
        // connection.setRequestProperty("Accept", "text/xml,text/javascript,text/html");
        // connection.setRequestProperty("User-Agent", "aop-sdk-java");

        return connection;
    }

    private static String getResponseAsString(HttpURLConnection connection) throws IOException {
        String charset = getResponseCharset(connection.getContentType());
        InputStream errorStream = connection.getErrorStream();
        if (errorStream == null) {
            return getStreamAsString(connection.getInputStream(), charset);
        } else {
            String msg = getStreamAsString(errorStream, charset);
            if (msg.length() == 0) {
                throw new IOException(connection.getResponseCode() + ":" + connection.getResponseMessage());
            }
            throw  new IOException(msg);
        }
    }

    private static String getResponseCharset(String contentType) {
        String charset = DEFAULT_CHARSET;

        if (!StringUtils.isEmpty(contentType)) {
            String[] params = contentType.split(";");
            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        if (!StringUtils.isEmpty(pair[1])) {
                            charset = pair[1].trim();
                        }
                    }
                    break;
                }
            }
        }

        return charset;
    }

    private static String getStreamAsString(InputStream stream, String charset) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringWriter writer = new StringWriter();

            char[] chars = new char[256];
            int count = 0;
            while ((count = reader.read(chars)) > 0) {
                writer.write(chars, 0, count);
            }

            return writer.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static void setHeaders(HttpURLConnection connection, Map<String, String> header) {
        List<String> keys = new ArrayList<String>(header.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = header.get(key);
            connection.setRequestProperty(key, value);
        }
    }
}
