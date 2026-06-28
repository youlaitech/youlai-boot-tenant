package com.youlai.boot.common.aspect;

import com.youlai.boot.common.annotation.IgnoreTenant;
import com.youlai.boot.framework.tenant.TenantContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 多租户切面，处理 @IgnoreTenant 注解
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Aspect
@Component
@Order(1)
public class TenantAspect {

    @Around("@annotation(ignoreTenant) || @within(ignoreTenant)")
    public Object around(ProceedingJoinPoint joinPoint, IgnoreTenant ignoreTenant) throws Throwable {
        try {
            TenantContextHolder.setIgnoreTenant(true);
            return joinPoint.proceed();
        } finally {
            TenantContextHolder.setIgnoreTenant(false);
        }
    }
}
