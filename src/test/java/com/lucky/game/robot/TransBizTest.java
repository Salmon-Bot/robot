package com.lucky.game.robot;

import com.lucky.game.robot.biz.DelteTransBiz;
import com.lucky.game.robot.biz.OrderBiz;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.biz.TransBiz;
import com.lucky.game.robot.dto.huobi.MaInfoDto;
import com.lucky.game.robot.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author conan
 *         2018/3/16 16:03
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TransBizTest {

    @Autowired
    TransBiz transBiz;

    @Autowired
    DelteTransBiz delteTransBiz;

    @Autowired
    OrderBiz orderBiz;

    @Test
    public void transTest() {
//        transBiz.trans("eoseth","eos",new BigDecimal(0.1),"1min",2,new BigDecimal(0.05));
    }

    @Test
    public void limitBuyOrderTest() {
        transBiz.hbTransModelLimitOrder();
    }


    @Test
    public void zbToSaleTest() {
        transBiz.zbToSale();
    }

    @Test
    public void zbCheckSaleFinishTest() {
        transBiz.zbCheckSaleFinish();
    }

    @Test
    public void zbTransModelLimitOrderTest() {
        transBiz.zbTransModelLimitOrder();
    }

    @Test
    @Rollback(false)
    public void hbCheckLimitBetaOrderTest() {
        transBiz.hbCheckLimitBetaOrder();
    }

    @Test
    @Rollback(false)
    public void zbCheckLimitBetaOrderTest() {
        transBiz.zbCheckLimitBetaOrder();
    }

    @Test
    @Rollback(false)
    public void hbCheckLimitGammaOrderBetaOrderTest() {
        transBiz.hbCheckLimitGammaOrder();
    }

    @Test
    @Rollback(false)
    public void calcMaTest() {
        delteTransBiz.calcMaInfo("eosusdt", DictEnum.MARKET_PERIOD_60MIN.getCode());
    }


    @Test
    public void checkByMaTest() {
        delteTransBiz.checkByMa();
    }

    @Test
    public void updateHbOrderStateTest() {
        OrderEntity orderEntity = orderBiz.findByBuyOrderId("4041693610");
        orderBiz.updateHbOrderState(orderEntity);
    }

    @Test
    public void getMaInfoTest() {
        MaInfoDto ma = transBiz.getMaInfo("eosusdt",DictEnum.MARKET_PERIOD_60MIN.getCode());
        System.err.println("ma="+ma);
    }

    @Test
    public void getDelteMaInfoTest() {
        MaInfoDto ma = delteTransBiz.getMaInfo("htusdt",DictEnum.MARKET_PERIOD_60MIN.getCode());
        System.err.println("ma="+ma);
    }

}
