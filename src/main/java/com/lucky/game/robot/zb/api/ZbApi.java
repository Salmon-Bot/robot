package com.lucky.game.robot.zb.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.lucky.game.core.util.StrRedisUtil;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.dto.zb.*;
import com.lucky.game.robot.zb.HttpUtilManager;
import com.lucky.game.robot.zb.MapSort;
import com.lucky.game.robot.zb.vo.*;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.zb.EncryDigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author conan
 *         2018/3/29 17:19
 **/
@Component
@Slf4j
public class ZbApi {

    @Value("${zb.trade.host:https://trade.bitkk.com/api/}")
    public String tradeHost;

    @Value("${zb.api.host:http://api.bitkk.com}")
    public String apiHost;

    @Autowired
    private RedisTemplate<String, String> redis;

    private static final String KLINE_KEY = "kline_key";

    @Value("${zb.stop.to.request:false}")
    private boolean stopToRequest;

    /**
     * 获取K线行情(每秒只能请求一次)
     */
    public ZbKineVo getKline(String currency, String type, Integer size) {
        ZbKineVo kineVo = new ZbKineVo();
        List<ZbKineDetailVo> detailVos = new ArrayList<>();
        // 请求地址
        String url = apiHost + "/data/v1/kline?market=" + currency + "&type=" + type + "&size=" + size;
        String klineKey = StrRedisUtil.get(redis, KLINE_KEY);
        if (StringUtils.isNotEmpty(klineKey)) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String callback = get(url);
        JSONObject json = JSONObject.parseObject(callback);
        StrRedisUtil.setEx(redis, KLINE_KEY, 2, KLINE_KEY);
        if (json == null || json.get("data") == null) {
            log.info("获取详情失败,url={},json={},currency={}", url, json, currency);
            return null;
        }
        Object[] objects = json.getJSONArray("data").toArray();
        for (Object object : objects) {
            JSONArray jsonArray = (JSONArray) object;
            ZbKineDetailVo vo = new ZbKineDetailVo();
            vo.setTimestamp(jsonArray.getLong(0));
            vo.setOpen(jsonArray.getBigDecimal(1));
            vo.setHigh(jsonArray.getBigDecimal(2));
            vo.setLow(jsonArray.getBigDecimal(3));
            vo.setClose(jsonArray.getBigDecimal(4));
            vo.setVol(jsonArray.getBigDecimal(5));
            detailVos.add(vo);
        }
        kineVo.setData(detailVos);
        kineVo.setMoneyType(json.getString("moneyType"));
        kineVo.setSymbol(json.getString("symbol"));
        return kineVo;
    }

    /**
     * 获取所有交易对
     */
    public List<ZbSymbolInfoVo> getSymbolInfo() {
        List<ZbSymbolInfoVo> zbSymbolInfoVoList = new ArrayList<>();
        // 请求地址
        String url = apiHost + "/data/v1/markets";
        String callback = get(url);
        JSONObject jsonObject = JSONObject.parseObject(callback);
        Set<Map.Entry<String, Object>> set = jsonObject.entrySet();
        for (Map.Entry<String, Object> aSet : set) {
            ZbSymbolInfoVo zbSymbolInfoVo = new ZbSymbolInfoVo();
            zbSymbolInfoVo.setCurrency(aSet.getKey());
            JSONObject json = (JSONObject) aSet.getValue();
            zbSymbolInfoVo.setAmountScale(json.getInteger("amountScale"));
            zbSymbolInfoVo.setPriceScale(json.getInteger("priceScale"));
            zbSymbolInfoVoList.add(zbSymbolInfoVo);
        }
        return zbSymbolInfoVoList;
    }

    /**
     * 委托下单
     */
    public ZbCreateOrderVo createOrder(ZbCreateOrderDto dto) {
        Map<String, String> params = new HashMap<>();
        params.put("method", "order");
        params.put("price", dto.getPrice());
        params.put("amount", dto.getAmount());
        params.put("tradeType", dto.getTradeType());
        params.put("currency", dto.getCurrency());
        return this.post(params, dto.getAccessKey(), dto.getSecretKey(), new TypeReference<ZbCreateOrderVo>() {
        });
    }

    /**
     * 取消下单
     */
    public ZbResponseVo cancelOrder(ZbCancelOrderDto dto) {
        Map<String, String> params = new HashMap<>();
        params.put("method", "cancelOrder");
        params.put("id", dto.getOrderId());
        params.put("currency", dto.getCurrency());
        return this.post(params, dto.getAccessKey(), dto.getSecretKey(), new TypeReference<ZbResponseVo>() {
        });
    }

    /**
     * 获取订单信息
     */
    public ZbOrderDetailVo orderDetail(ZbOrderDetailDto dto) {
        Map<String, String> params = new HashMap<>();
        params.put("method", "getOrder");
        params.put("id", dto.getOrderId());
        params.put("currency", dto.getCurrency());
        String json = this.getJsonPost(params, dto.getAccessKey(), dto.getSecretKey());
        Gson gson = new Gson();
        if (json != null && json.contains("attackIP")) {
            log.error("json={},dto={}", json, dto);
            return null;
        } else {
            try {
                return gson.fromJson(json, ZbOrderDetailVo.class);
            } catch (JsonSyntaxException e) {
                log.error("获取订单详情失败,json={},e={}", json, e);
                throw new BizException("获取订单详情失败");
            }
        }
    }


    /**
     * 获取深度
     */
    public ZbOrderDepthVo orderDepth(String currency, Integer size) {
        String url = apiHost + "/data/v1/depth?market=" + currency;
        if (size != null) {
            url = url + "&size=" + size;
        }
        return get(url, new TypeReference<ZbOrderDepthVo>() {
        });
    }

    /**
     * 最新行情数据
     */
    public ZbTickerVo getTicker(String currency) {
        String url = apiHost + "/data/v1/ticker?market=" + currency;
        ZbTickerResponseVo zbTickerResponseVo = get(url, new TypeReference<ZbTickerResponseVo>() {
        });
        return zbTickerResponseVo.getTicker();
    }

    /**
     * 获取个人信息
     */
    public List<ZbAccountDetailVo> getAccountInfo(BaseZbDto dto) {
        // 需加密的请求参数
        Map<String, String> params = new HashMap<>();
        params.put("method", "getAccountInfo");
        ZbAccountResponseVo accountResponseVo = this.post(params, dto.getAccessKey(), dto.getSecretKey(), new TypeReference<ZbAccountResponseVo>() {
        });
        if (accountResponseVo.getResult() == null) {
            log.error("用户账户信息错误,accountResponseVo={}", accountResponseVo);
            throw new BizException(ErrorEnum.USER_API_NOT_FOUND);
        }
        return accountResponseVo.getResult().getCoins();

    }

    /**
     * 提现
     */
    public ZbWithDrowVo withdraw(ZbWithDrawDto dto) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("amount", String.valueOf(dto.getAmount()));
        params.put("currency", dto.getCurrency());
        params.put("fees", dto.getFees().setScale(4, BigDecimal.ROUND_DOWN).toString());
        params.put("itransfer", dto.getItransfer());
        params.put("safePwd", dto.getSafePwd());
        params.put("method", "withdraw");
        params.put("receiveAddr", dto.getReceiveAddr());
        return this.post(params, dto.getAccessKey(), dto.getSecretKey(), new TypeReference<ZbWithDrowVo>() {
        });
    }

    /**
     * @return 返回指定类
     */

    private <T> T post(Map<String, String> params, String accessKey, String secretKey, TypeReference<T> ref) {
        try {
            String result = this.getJsonPost(params, accessKey, secretKey);
            return JsonUtil.readValue(result, ref);
        } catch (Exception e) {
            log.error("e={}", e);
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 获取json内容(统一加密)
     */
    private String getJsonPost(Map<String, String> params, String accessKey, String secretKey) {
        params.put("accesskey", accessKey);// 这个需要加入签名,放前面
        String digest = EncryDigestUtil.digest(secretKey);

        String sign = EncryDigestUtil.hmacSign(MapSort.toStringMap(params), digest); // 参数执行加密
        String method = params.get("method");

        // 加入验证
        params.put("sign", sign);
        params.put("reqTime", System.currentTimeMillis() + "");
        String json = "";
        try {
            if (!stopToRequest) {
                json = HttpUtilManager.getInstance().requestHttpPost(tradeHost, method, params);
            } else {
                log.warn("暂停对zb的访问");
                json = null;
            }
        } catch (HttpException | IOException e) {
            log.error("获取交易json异常", e);
        }
        return json;
    }

    /**
     * @param urlStr :请求接口
     * @return 返回指定类
     */
    private <T> T get(String urlStr, TypeReference<T> ref) {
        try {
            String result = this.get(urlStr);
            return JsonUtil.readValue(result, ref);
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    private String get(String urlStr) {
        BufferedReader reader;
        String result;
        StringBuilder sbf = new StringBuilder();
        String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";// 模拟浏览器
        try {
            if (!stopToRequest) {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(30000);
                connection.setConnectTimeout(30000);
                connection.setRequestProperty("User-agent", userAgent);
                connection.connect();
                InputStream is = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String strRead;
                while ((strRead = reader.readLine()) != null) {
                    sbf.append(strRead);
                    sbf.append("\r\n");
                }
                reader.close();
                result = sbf.toString();
            } else {
                log.warn("暂停对zb的访问");
                result = null;
            }

            return result;
        } catch (Exception e) {
            log.error("e={}", e);
            throw new BizException(e.getMessage());
        }
    }

}

class JsonUtil {

    public static String writeValue(Object obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    static <T> T readValue(String s, TypeReference<T> ref) throws IOException {
        return OBJECT_MAPPER.readValue(s, ref);
    }

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
