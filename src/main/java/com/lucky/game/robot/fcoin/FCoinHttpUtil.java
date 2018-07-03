package com.lucky.game.robot.fcoin;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 封装HTTP get post请求，简化发送http请求
 *
 * @author zhangchi
 */
@Slf4j
public class FCoinHttpUtil {

    private static FCoinHttpUtil instance = new FCoinHttpUtil();
    private static HttpClient client;
    private static long startTime = System.currentTimeMillis();
    public static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private static ConnectionKeepAliveStrategy keepAliveStrat = new DefaultConnectionKeepAliveStrategy() {
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            long keepAlive = super.getKeepAliveDuration(response, context);

            if (keepAlive == -1) {
                keepAlive = 5000;
            }
            return keepAlive;
        }

    };

    private FCoinHttpUtil() {
        client = HttpClients.custom().setConnectionManager(cm).setKeepAliveStrategy(keepAliveStrat).build();
    }

    public static void IdleConnectionMonitor() {

        if (System.currentTimeMillis() - startTime > 30000) {
            startTime = System.currentTimeMillis();
            cm.closeExpiredConnections();
            cm.closeIdleConnections(30, TimeUnit.SECONDS);
        }
    }

    private static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(20000).setConnectTimeout(20000)
            .setConnectionRequestTimeout(20000).build();

    public static FCoinHttpUtil getInstance() {
        return instance;
    }

    public HttpClient getHttpClient() {
        return client;
    }

    private HttpPost httpPostMethod(String url) {
        return new HttpPost(url);
    }

    private HttpRequestBase httpGetMethod(String url) {
        return new HttpGet(url);
    }

    public HttpResponse getResponse(String url) throws ClientProtocolException, IOException {
        IdleConnectionMonitor();
        HttpRequestBase method = this.httpGetMethod(url);

        method.setConfig(requestConfig);
        HttpResponse response = client.execute(method);
        return response;
    }



    public String get(String url) throws HttpException, IOException {

        IdleConnectionMonitor();
        HttpRequestBase method = this.httpGetMethod(url);
        method.setConfig(requestConfig);
        HttpResponse response = client.execute(method);

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return "";
        }
        InputStream is = null;
        String responseData = "";
        try {
            is = entity.getContent();
            responseData = IOUtils.toString(is, "UTF-8");
        } finally {
            if (is != null) {
                is.close();
            }
        }
        // System.out.println(responseData);
        return responseData;
    }



    public String fCoinGet(String url, String sign, String systemTime, String apiKey) throws HttpException, IOException {

        IdleConnectionMonitor();

        HttpRequestBase method = this.httpGetMethod(url);
        method.setHeader("FC-ACCESS-KEY",apiKey);
        method.setHeader("FC-ACCESS-SIGNATURE",sign);
        method.setHeader("FC-ACCESS-TIMESTAMP",systemTime);
        method.setConfig(requestConfig);
        HttpResponse response = client.execute(method);

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return "";
        }
        InputStream is = null;
        String responseData = "";
        try {
            is = entity.getContent();
            responseData = IOUtils.toString(is, "UTF-8");
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return responseData;
    }



    public String fCoinPost(String url,String sign,String systemTime,String apiKey,Map<String,String> params) throws HttpException, IOException {

        IdleConnectionMonitor();

        HttpPost httpPost = this.httpPostMethod(url);
        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
        httpPost.setHeader("FC-ACCESS-KEY",apiKey);
        httpPost.setHeader("FC-ACCESS-SIGNATURE",sign);
        httpPost.setHeader("FC-ACCESS-TIMESTAMP",systemTime);
        String jsonstr = JSON.toJSONString(params);
        StringEntity se = new StringEntity(jsonstr);
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(se);

        httpPost.setConfig(requestConfig);
        HttpResponse response = client.execute(httpPost);

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return "";
        }
        InputStream is = null;
        String responseData = "";
        try {
            is = entity.getContent();
            responseData = IOUtils.toString(is, "UTF-8");
        } finally {
            if (is != null) {
                is.close();
            }
        }
        // System.out.println(responseData);
        return responseData;
    }

    private List<NameValuePair> convertMap2PostParams(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        if (keys.isEmpty()) {
            return null;
        }
        int keySize = keys.size();
        List<NameValuePair> data = new LinkedList<NameValuePair>();
        for (int i = 0; i < keySize; i++) {
            String key = keys.get(i);
            String value = params.get(key);
            data.add(new BasicNameValuePair(key, value));
        }
        return data;
    }

}
