package com.lucky.game.robot;

import com.lucky.game.robot.biz.*;
import com.lucky.game.robot.dto.huobi.DepthDto;
import com.lucky.game.robot.entity.SymbolTradeConfigEntity;
import com.lucky.game.robot.entity.UserEntity;
import com.lucky.game.robot.huobi.response.Depth;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.schedule.MonitorSchedule;
import com.lucky.game.robot.vo.huobi.SymBolsDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * conan
 * 2017/10/18 9:51
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MarketMonitorBizTest {

    @Autowired
    private HbMarketMonitorBiz marketMonitorBiz;

    @Autowired
    private HuobiApi huobiApi;

    @Autowired
    private MonitorSchedule monitorSchedule;

    @Autowired
    private SymbolTradeConfigBiz symbolTradeConfigBiz;

    @Autowired
    private ZbMarketMonitorBiz zbMarketMonitorBiz;

    @Autowired
    private MarketBiz marketBiz;

    @Autowired
    private UserBiz userBiz;

    /**
     * 火币单种行情监控
     */
    @Test
    public void huoBiMonitorTest() throws Exception {
//        marketMonitorBiz.zbMonitor(DictEnum.MARKET_HUOBI_SYMBOL_BTC_USDT.getCode());
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet("https://api.huobipro.com/market/history/kline?period=1min&size=1&symbol=mtxbtc");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String jsonStr = EntityUtils.toString(entity, "utf-8");
        System.err.println(jsonStr);
    }

    /**
     * 火币全行情监控
     */
    @Test
    public void huoBiSymBolsTest() throws Exception {
        monitorSchedule.huoBiAllSymBolsMonitor();

    }


    /**
     * 火币交易对查询
     */
    @Test
    public void getSymbolsInfoTest() throws Exception {
        List<SymBolsDetailVo> list = huobiApi.getSymbolsInfo();
        log.info("list={}", list);

    }

    /**
     * send email
     */
    @Test
    public void sendEmailTest() throws Exception {
        List<UserEntity> list = userBiz.findAllHbByNormal();
        log.info("list={}", list);

    }


    @Test
    @Rollback(false)
    public void checkTransTest() {
        SymbolTradeConfigEntity symbolTradeConfigEntity = symbolTradeConfigBiz.findById("1");
        //increase eosbtc
//        marketMonitorBiz.checkToTrans("eoseth", new BigDecimal("0.1"), symbolTradeConfigEntity);

    }


    @Test
    @Rollback(false)
    public void zbMonitorTest() {
        try {
            zbMarketMonitorBiz.zbMonitor("eos_usdt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Rollback(false)
    public void zbAllSymBolsMonitorTest() {
        try {
            zbMarketMonitorBiz.initScaleToRedisAndMonitor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void HbDepthTest() {
        DepthDto dto = new DepthDto();
        dto.setSymbol("itcusdt");
        dto.setUserId("2c94a4ab624281b90162428266740001");
        Depth depth = marketBiz.HbDepth(dto);
        System.err.println("Depth=" + depth);
    }

}
