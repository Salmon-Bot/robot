package com.lucky.game.robot.aspect;

import com.lucky.game.core.aspect.BaseExceptionAspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class ExceptionAspect {

    @Pointcut("execution(* com.lucky.game.robot.controller..*(..))")
    public void aspect() {

    }

    @Around("aspect()")
    public Object around(JoinPoint joinPoint) {
        return BaseExceptionAspect.printExcLogAndReturn(joinPoint);
    }
}
