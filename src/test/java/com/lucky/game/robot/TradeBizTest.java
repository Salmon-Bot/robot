package com.lucky.game.robot;

import com.lucky.game.robot.biz.TradeBiz;
import com.lucky.game.robot.dto.huobi.CreateOrderDto;
import com.lucky.game.robot.dto.huobi.HuobiBaseDto;
import com.lucky.game.robot.huobi.response.OrdersDetail;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/14 14:52
 **/

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TradeBizTest {

    @Autowired
    private TradeBiz tradeBiz;

    @Test
    public void createOrderTest() {
        CreateOrderDto dto = new CreateOrderDto();
        dto.setAccountId("1229748");
        dto.setAmount(new BigDecimal(34.17605796));
        dto.setPrice(new BigDecimal(0.00117328));
        dto.setSymbol("elfeth");
        dto.setOrderType("buy-limit");
        String orderId = tradeBiz.hbCreateOrder(dto);
        log.info("orderId={}", orderId);
    }

    @Test
    public void orderDetailTest() {
        HuobiBaseDto dto = new HuobiBaseDto();
        dto.setOrderId("3797155374");
        dto.setUserId("2c94a4ab624281b90162428266740001");
        OrdersDetail detail = tradeBiz.getHbOrderDetail(dto);
        log.info("detail={}", detail);
    }

    @Test
    public void submitCancelTest() {
        HuobiBaseDto dto = new HuobiBaseDto();
        dto.setOrderId("3148680170");
        dto.setUserId("2c94a4ab624281b90162428266740001");
        tradeBiz.hbCancelOrder(dto);

    }
}
