package com.lucky.game.robot.fcoin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.fcoin.vo.FCoinDepthVo;
import com.lucky.game.robot.fcoin.vo.FCoinOrderDetailVo;
import com.lucky.game.robot.fcoin.vo.FCoinTickerVo;
import com.lucky.game.robot.huobi.response.Symbol;
import com.lucky.game.robot.util.HMAC_SHA1;
import com.lucky.game.robot.zb.MapSort;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * conan
 * 2018/7/1 下午12:40
 **/
@Slf4j
@Component
public class FCoinApi {


    @Value("${fcoin.api.host:https://api.fcoin.com}")
    public String apiHost;
//
//    @Value("${fcoin.api.key:1cd33987302f4b6ab80fd312521cb43f}")
//    public String key;
//
//    @Value("${fcoin.api.securt:efea04bc889040199972195c6bffb943}")
//    public String securt;


    /**
     * 获取交易对精度值
     */
    public Symbol getDecimal(String symbol) {
        Symbol symbolInfo = null;
        List<Symbol> allSymbol = getSymbols();
        for (Symbol vo : allSymbol) {
            if (vo.getSymbol().equals(symbol)) {
                symbolInfo = vo;
                break;
            }
        }
        return symbolInfo;
    }

    /**
     * 获取所有交易对
     */
    public List<Symbol> getSymbols() {
        List<Symbol> symbolList = new ArrayList<>();
        // 请求地址
        try {
            String url = apiHost + "/v2/public/symbols";
            String callback = FCoinHttpUtil.getInstance().get(url);
            JSONObject jsonObject = JSONObject.parseObject(callback);
            if (jsonObject == null) {
                log.info("获取信息失败,url={}", url);
                return null;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (Object object : jsonArray) {
                JSONObject result = (JSONObject) object;
                Symbol vo = new Symbol();
                vo.setSymbol(result.getString("name"));
                vo.setBaseCurrency(result.getString("base_currency"));
                vo.setQuoteCurrency(result.getString("quote_currency"));
                vo.setPriceDecimal(result.getInteger("price_decimal"));
                vo.setAmountDecimal(result.getInteger("amount_decimal"));
                symbolList.add(vo);
            }
        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return symbolList;
    }

    /**
     * 获取服务器时间戳
     */
    public String getServerTime() {
        // 请求地址
        try {
            String url = apiHost + "/v2/public/server-time";
            String callback = FCoinHttpUtil.getInstance().get(url);
            JSONObject jsonObject = JSONObject.parseObject(callback);
            if (jsonObject == null) {
                log.info("获取信息失败,url={}", url);
                return null;
            }
            return jsonObject.getString("data");
        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取交易深度
     *
     * @param level  L20,L100,full
     * @param symbol 交易对
     */
    public FCoinDepthVo getDepth(String level, String symbol) {
        // 请求地址
        try {
            String url = apiHost + "/v2/market/depth/" + level + "/" + symbol;
            String callback = FCoinHttpUtil.getInstance().get(url);
            if (callback == null) {
                log.info("获取信息失败,url={}", url);
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(callback);
            return jsonToObj(jsonObject.getString("data"), new TypeReference<FCoinDepthVo>() {
            });
        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取最新ticker信息
     */
    public FCoinTickerVo getTicker(String symbol) {
        // 请求地址
        try {
            String url = apiHost + "/v2/market/ticker/" + symbol;
            String callback = FCoinHttpUtil.getInstance().get(url);
            if (callback == null) {
                log.info("获取信息失败,url={}", url);
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(callback);
            return jsonToObj(jsonObject.getString("data"), new TypeReference<FCoinTickerVo>() {
            });
        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取订单详情
     *
     * @param orderId 订单id
     */
    public FCoinOrderDetailVo getDetail(String orderId, AccountEntity account) {
        // 请求地址
        try {
            String url = apiHost + "/v2/orders/" + orderId;
            String systemTime = getServerTime();
            String sign = getSignData(DictEnum.HTTP_GET.getCode(), url, systemTime, null,account);
            String result = FCoinHttpUtil.getInstance().fCoinGet(url, sign, systemTime, account.getApiKey());
            log.info(result);
            if (result == null) {
                log.info("获取信息失败,url={}", url);
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(result);
            return jsonToObj(jsonObject.getString("data"), new TypeReference<FCoinOrderDetailVo>() {
            });
        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 创建订单
     *
     * @param symbol 交易对
     * @param type   订单类型 limit 限价单
     * @param side   操作类型 buy/sell
     * @param amount 数量
     * @param price  价格
     */
    public String createOrder(String symbol, String type, String side, BigDecimal amount, BigDecimal price,AccountEntity account) {
        String result = null; // 参数执行加密
        try {
            Symbol symbolInfo = getDecimal(symbol);
            String url = apiHost + "/v2/orders/";
            Map<String, String> params = new HashMap<>();
            params.put("symbol", symbol);
            params.put("type", type);
            params.put("side", side);
            params.put("amount", amount.setScale(symbolInfo.getAmountDecimal(), BigDecimal.ROUND_DOWN).toString());
            params.put("price", price.setScale(symbolInfo.getPriceDecimal(), BigDecimal.ROUND_DOWN).toString());
            String systemTime = getServerTime();
            String sign = getSignData(DictEnum.HTTP_POST.getCode(), url, systemTime, params,account);
            result = FCoinHttpUtil.getInstance().fCoinPost(url, sign, systemTime, account.getApiKey(), params);
            log.info("result={}", result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (!"0".equals(jsonObject.getString("status"))) {
                throw new BizException("创建订单失败");
            }
            result = jsonObject.getString("data");
        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 删除订单
     */
    public String cancelOrder(String orderId,AccountEntity account) {
        String result = null; // 参数执行加密
        try {
            String url = apiHost + "/v2/orders/" + orderId + "/submit-cancel";
            String systemTime = getServerTime();
            String sign = getSignData(DictEnum.HTTP_POST.getCode(), url, systemTime, null,account);
            result = FCoinHttpUtil.getInstance().fCoinPost(url, sign, systemTime, account.getApiKey(), new HashMap<>());
            log.info("result={}", result);
        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 查询余额
     */
    public String balance(AccountEntity account) {
        String result = null; // 参数执行加密
        try {
            String url = apiHost + "/v2/accounts/balance";
            String systemTime = getServerTime();
            String sign = getSignData(DictEnum.HTTP_GET.getCode(), url, systemTime, null,account);
            result = FCoinHttpUtil.getInstance().fCoinGet(url, sign, systemTime, account.getApiKey());
            log.info("result={}", result);
        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 签名
     */
    public String getSignData(String httpMethod, String url, String systemTime, Map<String, String> params,AccountEntity account) {
        String toSignData;
        if (DictEnum.HTTP_GET.getCode().equals(httpMethod)) {
            toSignData = "GET" + url + systemTime;
        } else {
            toSignData = "POST" + url + systemTime;
        }
        if (params != null && params.size() > 0) {
            toSignData = toSignData + MapSort.toStringMap(params);
        }
        String actualSign = Base64.getEncoder().encodeToString(toSignData.getBytes());
        return HMAC_SHA1.genHMAC(actualSign, account.getApiSecret());

    }

    /**
     * @param result 返回数据
     * @return 返回指定类
     */
    private <T> T jsonToObj(String result, TypeReference<T> ref) {
        try {
            return JsonUtil.readValue(result, ref);
        } catch (Exception e) {
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
