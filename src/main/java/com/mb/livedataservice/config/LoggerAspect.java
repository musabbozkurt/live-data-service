package com.mb.livedataservice.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggerAspect {

    @Before("anyController() || anyService()")
    public void loggingBefore(JoinPoint joinPoint) {
        String methodName = getMethodName(joinPoint);
        String className = getClassName(joinPoint);
        Object[] args = joinPoint.getArgs();

        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            parameters.append("arg ").append(i + 1).append(": ").append(args[i]);
            parameters.append(" --- ");
        }

        log.info("called {} method of class {} with parameters {}", methodName, className, parameters);
    }

    @AfterReturning(pointcut = "anyController() || anyService()", returning = "returnValue")
    public void loggingAfter(JoinPoint joinPoint, Object returnValue) {
        String methodName = getMethodName(joinPoint);
        String className = getClassName(joinPoint);

        log.info("returned {} method of class {}", methodName, className);
    }

    @AfterThrowing(pointcut = "anyController() || anyService()", throwing = "e")
    public void loggingException(JoinPoint joinPoint, Exception e) {
        log.error("{} has thrown an error: ", getClassName(joinPoint), e);
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void anyController() {
    }

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void anyService() {
    }

    private String getMethodName(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
    }

    private String getClassName(JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getName();
    }
}
