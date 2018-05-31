package com.lucky.game.robot;

import com.lucky.game.robot.biz.ZbMarketMonitorBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 项目启动是自动加载项
 *
 * @author conan
 *         2018/3/23 13:29
 **/
@Component
public class MyCommandLineRunner implements CommandLineRunner {

    @Autowired
    private ZbMarketMonitorBiz zbMarketMonitorBiz;

    @Override
    public void run(String... var1) throws Exception {
//        zbMarketMonitorBiz.initScaleToRedisAndMonitor();
    }
}