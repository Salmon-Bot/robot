package com.lucky.game.robot.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LogAspect {

    @Pointcut("execution(* com.lucky.game.robot.biz.HbMarketMonitorBiz.asyncDoMonitor(..)) || " +
            "execution(* com.lucky.game.robot.schedule.MonitorSchedule.check*(..)) || " +
            "execution(* com.lucky.game.robot.biz.TransBiz.hbCreateModelLimitOrder(..))")
    public void aspect() {

    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try {
            MDC.put("logId", java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
        } catch (Throwable e) {
            log.error("e={}", e);
        }
        return pjp.proceed();
    }

    @After("aspect()")
    public void after() throws Throwable {
        try {
            MDC.remove("logId");
        } catch (Throwable e) {
            log.error("e={}", e);
        }
    }
}
