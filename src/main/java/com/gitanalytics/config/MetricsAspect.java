package com.gitanalytics.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(com.gitanalytics.config.TimedMetric)")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().toShortString();
        Timer timer = Timer.builder("gitanalytics.method.timed")
                .tag("method", methodName)
                .register(meterRegistry);

        return timer.recordCallable(() -> {
            try {
                return pjp.proceed();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }
}
