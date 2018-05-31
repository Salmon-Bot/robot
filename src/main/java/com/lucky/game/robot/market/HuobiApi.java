package com.lucky.game.robot.market;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.vo.huobi.MarketDetailVo;
import com.lucky.game.robot.vo.huobi.MarketInfoVo;
import com.lucky.game.robot.vo.huobi.SymBolsDetailVo;
import com.lucky.game.robot.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/3/8 17:53
 **/
@Component
@Slf4j
public class HuobiApi {


    @Value("${huobi.pro.api.host}")
    private String apiHost;
    /**
     * 行情
     */
    @Value("${huobi.pro.market.api.url:/market/history/kline}")
    private String marketApiUrl;

    /**
     * 交易对
     */
    @Value("${huobi.pro.api.symbol.url:/v1/common/symbols}")
    private String symbolsApiUrl;

    /**
     * 获取所有的交易对
     */
    public List<SymBolsDetailVo> getSymbolsInfo() {
        String jsonStr = null;
        List<SymBolsDetailVo> detailVoList = new ArrayList<>();
        try {
            CloseableHttpClient httpclient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(apiHost + symbolsApiUrl);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();//设置请求和传输超时时间
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            jsonStr = EntityUtils.toString(entity, "utf-8");
            httpGet.releaseConnection();
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            JSONArray detailArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < detailArray.size(); i++) {
                SymBolsDetailVo detailVo = new SymBolsDetailVo();
                //去除无效的交易对
                if (!"bt1".equals(detailArray.getJSONObject(i).getString("base-currency")) && !"bt2".equals(detailArray.getJSONObject(i).getString("base-currency"))) {
                    detailVo.setSymbols(detailArray.getJSONObject(i).getString("base-currency") + detailArray.getJSONObject(i).getString("quote-currency"));
                    detailVoList.add(detailVo);
                }
            }
            return detailVoList;
        } catch (Exception e) {
            log.warn("get all symbols fail,use default info,e={},result={}", e.getMessage(), e, jsonStr);
            List<DictEnum> dictEnumList = DictEnum.huobiSymbol;
            for (DictEnum dictEnum : dictEnumList) {
                SymBolsDetailVo detailVo = new SymBolsDetailVo();
                detailVo.setSymbols(dictEnum.getCode());
                detailVoList.add(detailVo);
            }
        }
        return detailVoList;
    }


    public MarketDetailVo getOneMarketDetail(String symbol) {
        MarketInfoVo infoVo = this.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 1, symbol);
        if (infoVo == null) {
            return null;
        }
        return infoVo.getData().get(0);
    }

    /**
     * @param period K线类型 1min, 5min, 15min, 30min, 60min, 1day, 1mon, 1week, 1year
     * @param size   获取数量 [1,2000]
     * @param symbol 交易对 btcusdt, bccbtc, rcneth
     */
    public synchronized MarketInfoVo getMarketInfo(String period, Integer size, String symbol) {
        String jsonStr = null;
        String url = null;
        try {
            //限制访问频率
            Thread.sleep(1000);
            CloseableHttpClient httpclient = HttpClientBuilder.create().build();
            //超过2000分钟，默认查询100条
            if (size > 2000) {
                size = 2000;
            }
            if (StringUtils.isEmpty(symbol)) {
                log.error("symbol is not null");
                return null;
            }
            //增加AccessKeyId 据hb工作人员所说可以解决429 too many request 的问题
            url = apiHost + marketApiUrl + "?period=" + period + "&size=" + size + "&symbol=" + symbol +"&AccessKeyId=3d008259-3cc04933-0a10846b-07ac1";
            HttpGet httpGet = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();//设置请求和传输超时时间
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            jsonStr = EntityUtils.toString(entity, "utf-8");
            httpGet.releaseConnection();
            JSONObject json = JSON.parseObject(jsonStr);
            if (!"ok".equals(json.getString("status"))) {
                log.info(symbol + " market info not found. result={}", json);
                return null;
            }
            MarketInfoVo marketInfoVo = new ObjectMapper().readValue(jsonStr, MarketInfoVo.class);
            if (marketInfoVo.getData() == null || marketInfoVo.getData().isEmpty()) {
                throw new BizException(ErrorEnum.MARKEY_INFO_FAIL);
            }
            return marketInfoVo;
        } catch (Exception e) {
            if(jsonStr != null && jsonStr.contains("429 Too Many Requests")){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            log.error("get market fail,url={},result={},period={},size={},symbol={},e={},", url, jsonStr, period, size, symbol, e.getMessage());
            return null;
        }
    }
}


