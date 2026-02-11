package com.back.common.annotation;

import com.back.common.aop.LoggingAspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 어노테이션은 메서드 실행 시 로깅을 적용할 메서드를 표시하는 데 사용됩니다.
 * AOP {@link LoggingAspect} 에 의해 처리됩니다.
 *
 * 이 어노테이션의 속성을 통해 로깅 동작을 세밀하게 제어할 수 있습니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {

    /**
     * 해당 메서드의 로깅 레벨을 지정합니다.
     * 기본값은 INFO입니다.
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * 메서드 인자의 로깅 여부를 지정합니다.
     * 민감한 정보가 포함된 메서드의 경우 false로 설정할 수 있습니다.
     * 기본값은 true입니다.
     */
    boolean logArgs() default true;

    /**
     * 메서드 반환 값의 로깅 여부를 지정합니다.
     * 민감한 정보나 너무 큰 객체를 반환하는 메서드의 경우 false로 설정할 수 있습니다.
     * 기본값은 true입니다.
     */
    boolean logResult() default true;

    /**
     * 메서드 실행 시간이 이 임계값(밀리초)을 초과할 경우 WARN 레벨로 로그를 남깁니다.
     * 0 이하의 값은 임계값을 사용하지 않음을 의미합니다.
     * 기본값은 -1입니다.
     */
    long warnThresholdMillis() default -1;
}