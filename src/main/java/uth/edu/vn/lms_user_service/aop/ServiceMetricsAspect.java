package uth.edu.vn.lms_user_service.aop;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceMetricsAspect {

    private final MeterRegistry meterRegistry;

    public ServiceMetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* uth.edu.vn.lms_user_service.service.*.*(..))")
    public Object measureServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Object result = joinPoint.proceed();
            
            Counter.builder("service.calls")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "success")
                    .register(meterRegistry)
                    .increment();
            
            return result;
        } catch (Exception e) {
            Counter.builder("service.calls")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "error")
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("service.timer")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry));
        }
    }
}
