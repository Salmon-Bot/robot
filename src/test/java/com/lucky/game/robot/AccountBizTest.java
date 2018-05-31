package com.lucky.game.robot;

import com.lucky.game.robot.biz.AccountBiz;
import com.lucky.game.robot.vo.BalanceVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author conan
 *         2018/3/14 14:52
 **/

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AccountBizTest {

    @Autowired
    private AccountBiz accountBiz;

    @Test
    public void getUserBaseCurrencyBalanceTest() {
        BalanceVo vo = accountBiz.getUserBaseCurrencyBalance("2c94a4ab624281b90162428266741111");
        log.info("vo={}", vo);
    }


}
