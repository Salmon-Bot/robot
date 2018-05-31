package com.lucky.game.robot;

import com.lucky.game.robot.dto.zb.ZbCancelOrderDto;
import com.lucky.game.robot.dto.zb.ZbWithDrawDto;
import com.lucky.game.robot.zb.HttpUtilManager;
import com.lucky.game.robot.zb.MapSort;
import com.lucky.game.robot.zb.vo.ZbResponseVo;
import com.lucky.game.robot.zb.vo.ZbWithDrowVo;
import com.lucky.game.robot.dto.zb.BaseZbDto;
import com.lucky.game.robot.dto.zb.ZbOrderDetailDto;
import com.lucky.game.robot.zb.EncryDigestUtil;
import com.lucky.game.robot.zb.api.ZbApi;
import com.lucky.game.robot.zb.vo.ZbOrderDepthVo;
import com.lucky.game.robot.zb.vo.ZbOrderDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ZBRestTest {


    @Autowired
    ZbApi zbApi;

    // 正式
    public final String ACCESS_KEY = "7c08f59f-1b32-4cd8-ab8e-cb32564f6e12";
    public final String SECRET_KEY = "3ad4640f-d2f5-4fa1-bedb-9752e1e38284";
    public final String URL_PREFIX = "https://trade.bitkk.com/api/";
    public static String API_DOMAIN = "http://api.bitkk.com";


    public final String PAY_PASS = "xxxx";

    /**
     * 委托下单 tradeType 1买，0卖
     */
    @Test
    public void testOrder() {
        try {
            // 需加密的请求参数， tradeType=0卖单
            Map<String, String> params = new HashMap<String, String>();
            params.put("method", "order");
            params.put("price", "13.19");
            params.put("amount", "4.630");
            params.put("tradeType", "1");
            params.put("currency", "sbtc_usdt");

            // 请求测试
            String json = this.getJsonPost(params);
            System.out.println("testOrder 结果: " + json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 获取订单信息
     */
    @Test
    public void testGetOrder() {
        String orderId = "2018042855179172";
        try {
            ZbOrderDetailDto dto = new ZbOrderDetailDto();
            dto.setCurrency("eos_usdt");
            dto.setOrderId(orderId);
            dto.setAccessKey("7c08f59f-1b32-4cd8-ab8e-cb32564f6e12");
            dto.setSecretKey("3ad4640f-d2f5-4fa1-bedb-9752e1e38284");
            ZbOrderDetailVo vo = zbApi.orderDetail(dto);
            log.info("vo={}",vo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 获取订单信息
     */
    @Test
    public void testCancelOrder() {
        String orderId = "201804091098273";
        try {
            ZbCancelOrderDto dto = new ZbCancelOrderDto();
            dto.setCurrency("sbtc_btc");
            dto.setOrderId(orderId);
            dto.setAccessKey("7c08f59f-1b32-4cd8-ab8e-cb32564f6e12");
            dto.setSecretKey("3ad4640f-d2f5-4fa1-bedb-9752e1e38284");
            ZbResponseVo vo = zbApi.cancelOrder(dto);
            System.err.println("vo=" + vo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取多个委托买单或卖单，每次请求返回10条记录
     */
//	 @Test
    public void testGetOrders() {
        try {
            String[] currencyArr = new String[]{"ltc_btc", "eth_btc", "etc_btc"};
            for (String currency : currencyArr) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("method", "getOrders");
                params.put("tradeType", "1");
                params.put("currency", currency);
                params.put("pageIndex", "1");

                String json = this.getJsonPost(params);
                log.info("testGetOrders 结果: " + json);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * (新)获取多个委托买单或卖单，每次请求返回pageSize<=100条记录
     */
//	 @Test
    public void testGetOrdersNew() {
        try {
            String[] currencyArr = new String[]{"ltc_btc", "eth_btc", "etc_btc"};
            for (String currency : currencyArr) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("method", "getOrdersNew");
                params.put("tradeType", "1");
                params.put("currency", currency);
                params.put("pageIndex", "1");
                params.put("pageSize", "1");
                String json = this.getJsonPost(params);
                log.info("testGetOrdersNew 结果: " + json);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 与getOrdersNew的区别是取消tradeType字段过滤，可同时获取买单和卖单，每次请求返回pageSize<=100条记录
     */
    // @Test
    public void getOrdersIgnoreTradeType() {
        try {
            String[] currencyArr = new String[]{"ltc_btc", "eth_btc", "etc_btc"};
            for (String currency : currencyArr) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("method", "getOrdersIgnoreTradeType");
                params.put("currency", currency);
                params.put("pageIndex", "1");
                params.put("pageSize", "1");

                String json = this.getJsonPost(params);
                log.info("getOrdersIgnoreTradeType 结果: " + json);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 获取未成交或部份成交的买单和卖单，每次请求返回pageSize<=100条记录
     */
    // @Test
    public void getUnfinishedOrdersIgnoreTradeType() {
        try {
            String[] currencyArr = new String[]{"ltc_btc", "eth_btc", "etc_btc"};
            for (String currency : currencyArr) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("method", "getUnfinishedOrdersIgnoreTradeType");
                params.put("currency", currency);
                params.put("pageIndex", "1");
                params.put("pageSize", "10");

                String json = this.getJsonPost(params);
                log.info("getUnfinishedOrdersIgnoreTradeType 结果: " + json);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取个人信息
     */
    @Test
    public void testGetAccountInfo() {
        BaseZbDto dto = new BaseZbDto();
        dto.setAccessKey("7c08f59f-1b32-4cd8-ab8e-cb32564f6e12");
        dto.setSecretKey("3ad4640f-d2f5-4fa1-bedb-9752e1e38284");
        zbApi.getAccountInfo(new BaseZbDto());
    }

    /**
     * 获取个人的充值地址
     */
//	@Test
    public void testGetUserAddress() {
        try {
            // 需加密的请求参数
            Map<String, String> params = new HashMap<String, String>();
            params.put("method", "getUserAddress");
            params.put("currency", "btc");
            String json = this.getJsonPost(params);
            System.out.println("getUserAddress 结果: " + json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取认证的提现地址
     */
//	@Test
    public void testGetWithdrawAddress() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("method", "getWithdrawAddress");
            params.put("currency", "etc");

            String json = this.getJsonPost(params);
            System.out.println("getWithdrawAddress 结果: " + json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取提现记录
     */
//	@Test
    public void testGetWithdrawRecord() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("method", "getWithdrawRecord");
            params.put("currency", "eth");
            params.put("pageIndex", "1");
            params.put("pageSize", "10");
            String json = this.getJsonPost(params);
            System.out.println("getWithdrawRecord 结果: " + json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取虚拟货币充值记录
     */
//	@Test
    public void testGetChargeRecord() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("method", "getChargeRecord");
            params.put("currency", "btc");
            params.put("pageIndex", "1");
            params.put("pageSize", "10");
            String json = this.getJsonPost(params);

            System.out.println("getChargeRecord 结果: " + json);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 提现操作
     */
    @Test
    public void withdraw() {
        try {

            ZbWithDrawDto dto = new ZbWithDrawDto();
            dto.setReceiveAddr("huobi-pro_112300");
            dto.setFees(new BigDecimal(3));
            dto.setCurrency("bts");
            dto.setAmount(new BigDecimal(10));
            dto.setAccessKey("7c08f59f-1b32-4cd8-ab8e-cb32564f6e12");
            dto.setSecretKey("3ad4640f-d2f5-4fa1-bedb-9752e1e38284");
            dto.setSafePwd("824968");
            ZbWithDrowVo vo = zbApi.withdraw(dto);
            log.info("withdraw 结果: " + vo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 测试获取行情
     */
//	@Test
    public void testTicker() {
        try {
            String currency = "ltc_btc";
            // 请求地址
            String url = API_DOMAIN + "/data/v1/ticker?market=" + currency;
            log.info(currency + "-testTicker url: " + url);
            // 请求测试
            String callback = get(url, "UTF-8");
            log.info(currency + "-testTicker 结果: " + callback);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 测试获取深度
     */
    @Test
    public void testDepth() {
        ZbOrderDepthVo orderDepthVo = zbApi.orderDepth("btc_usdt", 3);
        log.info("OrderDepthVo={}", orderDepthVo);
    }

    /**
     * 测试获取最近交易记录
     */
//	@Test
    public void testTrades() {
        try {
            String currency = "etc_btc";
            // 请求地址
            String url = API_DOMAIN + "/data/v1/trades?market=" + currency;
            log.info(currency + "-testTrades url: " + url);
            // 请求测试
            String callback = get(url, "UTF-8");
            log.info(currency + "-testTrades 结果: " + callback);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 测试获取K线数据
     */
    @Test
    public void testKline() {
        try {
            zbApi.getKline("btc_usdt", "1min", 6);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 测试获取K线数据
     */
    @Test
    public void testMarketInfo() {
        try {
            zbApi.getSymbolInfo();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 获取json内容(统一加密)
     *
     * @param params
     * @return
     */
    private String getJsonPost(Map<String, String> params) {
        params.put("accesskey", ACCESS_KEY);// 这个需要加入签名,放前面
        String digest = EncryDigestUtil.digest(SECRET_KEY);

        String sign = EncryDigestUtil.hmacSign(MapSort.toStringMap(params), digest); // 参数执行加密
        String method = params.get("method");

        // 加入验证
        params.put("sign", sign);
        params.put("reqTime", System.currentTimeMillis() + "");
        String url = "请求地址:" + URL_PREFIX + method + " 参数:" + params;
        System.out.println(url);
        String json = "";
        try {
            json = HttpUtilManager.getInstance().requestHttpPost(URL_PREFIX, method, params);
        } catch (HttpException | IOException e) {
            log.error("获取交易json异常", e);
        }
        return json;
    }

    /**
     * @param urlAll  :请求接口
     * @param charset :字符编码
     * @return 返回json结果
     */
    public String get(String urlAll, String charset) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";// 模拟浏览器
        try {
            URL url = new URL(urlAll);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            connection.setRequestProperty("User-agent", userAgent);
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, charset));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
