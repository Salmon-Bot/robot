package com.lucky.game.robot;

import com.lucky.game.robot.biz.TrendBizz;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * conan
 * 2018/7/24 上午11:19
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TrendBizzTest {

    @Autowired
    private TrendBizz trendBizz;

    @Test
    public void kdjTest(){
        trendBizz.kdj("btcusdt");
    }
}
