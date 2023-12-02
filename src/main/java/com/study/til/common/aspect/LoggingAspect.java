package com.study.til.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Slf4j
@Component
public class LoggingAspect {
    @Around("execution(* com.study.til.common.advice..*(..))")
    public Object requestWarnLogging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        long startAt = System.currentTimeMillis();
        Object returnValue = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        long endAt = System.currentTimeMillis();
        log.warn("================================================NEW===============================================");
        log.warn("====> Request: {} {} ({}ms)\n *Header = {}", request.getMethod(), request.getRequestURL(), endAt - startAt, getHeaders(request));
        if (returnValue != null) {
            log.warn("====> Response: {}", returnValue);
        }
        log.warn("================================================END===============================================");
        return returnValue;
    }

    @Around("execution(* com.study.til.controller..*(..))")
    public Object requestLogging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        long startAt = System.currentTimeMillis();
        Object returnValue = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        long endAt = System.currentTimeMillis();
        log.info("================================================NEW===============================================");
        log.info("====> Request: {} {} ({}ms)\n *Header = {}", request.getMethod(), request.getRequestURL(), endAt - startAt, getHeaders(request));
        if (returnValue != null) {
            log.info("====> Response: {}", returnValue);
        }
        log.info("================================================END===============================================");
        return returnValue;
    }

    private Map<String, Object> getHeaders(HttpServletRequest request) {
        Map<String, Object> headerMap = new HashMap<>();

        Enumeration<String> headerArray = request.getHeaderNames();
        while (headerArray.hasMoreElements()) {
            String headerName = headerArray.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }
}
