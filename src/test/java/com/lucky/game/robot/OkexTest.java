package com.lucky.game.robot;

import com.lucky.game.robot.bian.OkexApi;
import com.lucky.game.robot.zb.vo.ZbKineVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class OkexTest {


    @Autowired
    OkexApi okexApi;

    /**
     *
     */
    @Test
    public void testKline() {
        try {
            ZbKineVo zbKineVo = okexApi.getKline("btcusdt", "1hour", 10);
            System.out.println("testKline 结果: " + zbKineVo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
