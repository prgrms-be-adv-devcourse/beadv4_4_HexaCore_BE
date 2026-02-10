package com.back.common.aop;

import com.back.common.annotation.LogLevel;
import com.back.common.annotation.Loggable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("@annotation(com.back.common.annotation.Loggable)")
    public void loggableMethods() {}

    @Around("loggableMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable loggable = method.getAnnotation(Loggable.class);

        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        LogLevel logLevel = loggable.level();
        boolean logArgs = loggable.logArgs();
        boolean logResult = loggable.logResult();
        long warnThresholdMillis = loggable.warnThresholdMillis();

        String argsString = logArgs ? Arrays.toString(args) : "[인자 로깅 비활성화]";

        logAtLevel(logLevel, log, ">>>> {}.{}() 호출. Args: {}", className, methodName, argsString);

        Object result = null;
        long startTime = System.currentTimeMillis();
        try {
            result = joinPoint.proceed(); // Execute the method
            return result;
        } catch (IllegalArgumentException e) {
            log.error("#### {}.{}() 에서 잘못된 인자: {} in {}", className, methodName, argsString, e.getMessage(), e);
            throw e;
        } catch (Throwable e) {
            log.error("#### {}.{}() 에서 예외 발생: {}", className, methodName, e.getMessage(), e);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            String resultString = logResult ? String.valueOf(result) : "[반환 값 로깅 비활성화]";

            logAtLevel(logLevel, log, "<<<< {}.{}() 종료. 실행 시간: {}ms. 반환 값: {}", className, methodName, executionTime, resultString);

            if (warnThresholdMillis > 0 && executionTime > warnThresholdMillis) {
                log.warn("#### {}.{}() 경고: 실행 시간이 임계값 {}ms를 초과했습니다 ({}ms).", className, methodName, warnThresholdMillis, executionTime);
            }
        }
    }

    private void logAtLevel(LogLevel level, Logger logger, String format, Object... arguments) {
        switch (level) {
            case TRACE:
                if (logger.isTraceEnabled()) logger.trace(format, arguments);
                break;
            case DEBUG:
                if (logger.isDebugEnabled()) logger.debug(format, arguments);
                break;
            case INFO:
                if (logger.isInfoEnabled()) logger.info(format, arguments);
                break;
            case WARN:
                if (logger.isWarnEnabled()) logger.warn(format, arguments);
                break;
            case ERROR:
                if (logger.isErrorEnabled()) logger.error(format, arguments);
                break;
        }
    }
}
