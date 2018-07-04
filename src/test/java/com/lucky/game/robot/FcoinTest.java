package com.lucky.game.robot;

import com.lucky.game.robot.biz.AccountBiz;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.fcoin.FCoinApi;
import com.lucky.game.robot.fcoin.vo.FCoinDepthVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

/**
 * conan
 * 2018/7/1 下午12:32
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class FcoinTest {

    @Autowired
    private FCoinApi fCoinApi;

    @Autowired
    private AccountBiz accountBiz;


    @Test
    public void testGetSymbols(){
        fCoinApi.getSymbols();
    }

    @Test
    public void testCreateOrder(){
        AccountEntity account = accountBiz.getByUserIdAndType("2c94a4ab624281b90162428266740001", DictEnum.MARKET_TYPE_FCOIN.getCode());
        fCoinApi.createOrder("btcusdt","limit,","buy",new BigDecimal(1),new BigDecimal(1),account);
    }

    @Test
    public void testCancelOrder(){
        AccountEntity account = accountBiz.getByUserIdAndType("2c94a4ab624281b90162428266740001", DictEnum.MARKET_TYPE_FCOIN.getCode());
        fCoinApi.cancelOrder("1",account);
    }


    @Test
    public void testGetDepth(){
        FCoinDepthVo vo = fCoinApi.getDepth("L20","btcusdt");
        log.info("vo={}",vo);
    }

    @Test
    public void testBalance(){

        AccountEntity account = accountBiz.getByUserIdAndType("2c94a4ab624281b90162428266740001", DictEnum.MARKET_TYPE_FCOIN.getCode());
        fCoinApi.balance(account);
    }

}
