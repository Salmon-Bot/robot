package com.lucky.game.robot.sms;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author conan
 *         2018/3/13 13:50
 **/
@Slf4j
public class Sms {

    private static String smsUrl = "http://115.28.143.178:8080/sms/Send.do";

    private static String spId = "5427";

    private static String loginName = "ddkwlkj";

    private static String password = "ddkwlkjbnmkg";

    public static String smsSend(String content, String mobiles) {
        Map<String, String> map = new HashMap<>();
        map.put("spId", spId);
        map.put("loginName", loginName);
        map.put("password", password);
        map.put("mobiles", mobiles);
        map.put("content", content);
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (String key : map.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, map.get(key)));
        }

        return smsSend(nameValuePairs);
    }

    public static String smsSend(List<NameValuePair> params) {
        String jsonStr = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(10000).setConnectionRequestTimeout(10000)
                    .setSocketTimeout(30000).build();
            CloseableHttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost(smsUrl);
            httppost.setConfig(requestConfig);
            httppost.setEntity(new UrlEncodedFormEntity(params, "GBK"));
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            jsonStr = EntityUtils.toString(entity, "GBK");
            httppost.releaseConnection();
            log.info("send sms success.result={}", jsonStr);
        } catch (Exception e) {
            log.error("send sms fail ...e={}", e);
        }
        return jsonStr;
    }

}

