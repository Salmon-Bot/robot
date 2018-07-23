package com.lucky.game.robot;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.huobi.api.ApiClient;
import com.lucky.game.robot.huobi.request.WithDrawRequest;
import com.lucky.game.robot.huobi.response.DepositWithdrawResponse;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.vo.huobi.MarketInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author conan
 *         2018/4/12 17:19
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class HuobiApiTest {

    @Autowired
    private HuobiApi huobiApi;

    @Test
    public void withdrawTest() {
        ApiClient client = new ApiClient("3d008259-3cc04933-0a10846b-07ac1", "c46a6b5d-fb92da4b-8ae68067-45c21");
        WithDrawRequest withDrawRequest = new WithDrawRequest();
        withDrawRequest.setCurrency("bts");
        withDrawRequest.setAmount("10");
        withDrawRequest.setAddress("0x171e70136b8a51ff8110236ab7980f8ee4ee0129");
        client.withdraw(withDrawRequest);
    }

    @Test
    public void depositWithdrawTest() {
        ApiClient client = new ApiClient("3d008259-3cc04933-0a10846b-07ac1", "c46a6b5d-fb92da4b-8ae68067-45c21");
        List<DepositWithdrawResponse> response = client.depositWithdraw("bts", "deposit");
        log.info("response={}", response);
    }

    @Test
    public void test(){
        MarketInfoVo info = huobiApi.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 1, "htusdt");
        if(new BigDecimal(3.5658).multiply(new BigDecimal(1).subtract(new BigDecimal(0.05))).compareTo(info.getData().get(0).getClose()) <= 0){
            log.info("aaa");
        }
        log.info("bbb");
    }
}
