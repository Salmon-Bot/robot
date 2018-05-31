package com.lucky.game.robot;

import com.lucky.game.robot.biz.ShuffleBiz;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author conan
 *         2018/4/11 16:39
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ShuffleBizTest {

    @Autowired
    private ShuffleBiz shuffleBiz;

    @Test
    @Rollback
    public void monitorShuffleTest() {
        shuffleBiz.shuffleMonitor();
    }
}
