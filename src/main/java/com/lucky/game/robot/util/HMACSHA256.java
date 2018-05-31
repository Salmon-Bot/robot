package com.lucky.game.robot.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @author conan
 *         2018/3/13 10:38
 **/
public class HMACSHA256 {
    //   SECRET KEY
    private final static String secret_key = "e8100b66-4b21e54a-1c78079c-558a9";

    /**
     * 将加密后的字节数组转换成字符串
     *
     * @param b 字节数组
     * @return 字符串
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    /**
     * sha256_HMAC加密
     *
     * @param message 消息
     * @param secret  秘钥
     * @return 加密后字符串
     */
    private static String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            hash =  Base64.getEncoder().encodeToString(bytes);
            System.out.println("hash="+hash);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }

    public static void main(String[] s) {
        String param = "GET\n" +
                "api.huobi.pro\n" +
                "/v1/account/accounts\n" +
                "AccessKeyId=3ab5bb27-6333-4d85-a003-e38256a8e765&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2018-03-13T09%3A34%3A02";
        String hash = sha256_HMAC(param, secret_key);
        System.err.println("base value="+hash);
    }

}
