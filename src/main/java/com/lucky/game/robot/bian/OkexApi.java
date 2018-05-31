package com.lucky.game.robot.bian;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.zb.vo.ZbKineVo;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.zb.vo.ZbKineDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/3/29 17:19
 **/
@Component
@Slf4j
public class OkexApi {

    @Value("${bian.api.host:https://www.okex.com}")
    public String apiHost;

    /**
     * 获取K线
     */
    public ZbKineVo getKline(String symbol, String type, Integer size) {
        symbol = changeSymbol(symbol);
        ZbKineVo kineVo = new ZbKineVo();
        List<ZbKineDetailVo> detailVos = new ArrayList<>();
        // 请求地址
        String url = apiHost + "/api/v1/kline.do?symbol=" + symbol + "&type=" + type + "&size=" + size;
        String callback = get(url);
        JSONArray jsonArray = JSONObject.parseArray(callback);
        if (jsonArray == null) {
            log.info("获取信息失败,url={},json={},currency={}", url, callback, symbol);
            return null;
        }
        for (Object object : jsonArray) {
            JSONArray resultArray = (JSONArray) object;
            ZbKineDetailVo vo = new ZbKineDetailVo();
            vo.setTimestamp(resultArray.getLong(0));
            vo.setOpen(resultArray.getBigDecimal(1));
            vo.setHigh(resultArray.getBigDecimal(2));
            vo.setLow(resultArray.getBigDecimal(3));
            vo.setClose(resultArray.getBigDecimal(4));
            vo.setVol(resultArray.getBigDecimal(5));
            detailVos.add(vo);
        }
        kineVo.setData(detailVos);
        return kineVo;
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
        String result=null;
        StringBuilder sbf = new StringBuilder();
        String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";// 模拟浏览器
        try {
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
            return result;
        } catch (Exception e) {
            log.error("e={}",e);
            throw new BizException(e.getMessage());
        }
    }


    /**
     * 交易对转换
     */
    private String changeSymbol(String symbol) {
        if (symbol.endsWith(DictEnum.HB_MARKET_BASE_USDT.getCode())){
            symbol= symbol.replace("usdt","_usdt");
        }else if(symbol.endsWith(DictEnum.HB_MARKET_BASE_BTC.getCode())){
            symbol= symbol.replace("btc","_btc");
        }else if(symbol.endsWith(DictEnum.HB_MARKET_BASE_ETH.getCode())){
            symbol= symbol.replace("eth","_eth");
        }

        return symbol;
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


