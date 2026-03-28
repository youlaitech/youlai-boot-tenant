package com.youlai.boot.common.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.enums.ActionTypeEnum;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.framework.tenant.TenantContextHolder;
import com.youlai.boot.common.util.IPUtils;
import com.youlai.boot.framework.security.util.SecurityUtils;
import com.youlai.boot.system.model.entity.SysLog;
import com.youlai.boot.system.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {
    private final LogService logService;
    private final HttpServletRequest request;
    private final CacheManager cacheManager;

    @Pointcut("@annotation(com.youlai.boot.common.annotation.Log)")
    public void logPointcut() {
    }

    @Around("logPointcut() && @annotation(logAnno)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log logAnno) throws Throwable {
        TimeInterval timer = DateUtil.timer();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long executionTime = timer.interval();
            Long userId = SecurityUtils.getUserId();
            String username = SecurityUtils.getUsername();
            this.saveLog(logAnno, exception, executionTime, userId, username);
        }
        return result;
    }

    private void saveLog(Log logAnno, Exception exception, long executionTime, Long userId, String username) {
        SysLog sysLog = new SysLog();

        LogModuleEnum module = logAnno.module();
        ActionTypeEnum actionType = logAnno.value();
        String title = StrUtil.blankToDefault(logAnno.title(),
                module.getLabel() + "-" + actionType.getLabel());
        String content = logAnno.content();

        sysLog.setModule(module);
        sysLog.setActionType(actionType);
        sysLog.setTitle(title);
        sysLog.setContent(content);
        sysLog.setOperatorId(userId);
        sysLog.setOperatorName(username);
        sysLog.setStatus(exception == null ? 1 : 0);
        sysLog.setErrorMsg(exception != null ? exception.getMessage() : null);
        sysLog.setExecutionTime((int) executionTime);
        sysLog.setRequestUri(request.getRequestURI());
        sysLog.setRequestMethod(request.getMethod());

        String ipAddr = IPUtils.getIpAddr(request);
        if (StrUtil.isNotBlank(ipAddr)) {
            sysLog.setIp(ipAddr);
            String region = IPUtils.getRegion(ipAddr);
            if (StrUtil.isNotBlank(region)) {
                String[] regionArray = region.split("\\|");
                if (regionArray.length > 2) {
                    sysLog.setProvince(regionArray[2]);
                    sysLog.setCity(regionArray[3]);
                }
            }
        }

        String userAgentString = request.getHeader("User-Agent");
        UserAgent userAgent = resolveUserAgent(userAgentString);
        if (Objects.nonNull(userAgent)) {
            sysLog.setOs(userAgent.getOs().getName());
            sysLog.setBrowser(userAgent.getBrowser().getName());
            sysLog.setDevice(userAgent.getPlatform().getName());
        }

        boolean ignoreTenantBefore = TenantContextHolder.isIgnoreTenant();
        try {
            if (TenantContextHolder.getTenantId() == null) {
                TenantContextHolder.setIgnoreTenant(true);
            }
            logService.save(sysLog);
        } finally {
            TenantContextHolder.setIgnoreTenant(ignoreTenantBefore);
        }
    }

    public UserAgent resolveUserAgent(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return null;
        }
        String userAgentStringMD5 = DigestUtil.md5Hex(userAgentString);
        UserAgent userAgent = Objects.requireNonNull(cacheManager.getCache("userAgent")).get(userAgentStringMD5, UserAgent.class);
        if (userAgent != null) {
            return userAgent;
        }
        userAgent = UserAgentUtil.parse(userAgentString);
        Objects.requireNonNull(cacheManager.getCache("userAgent")).put(userAgentStringMD5, userAgent);
        return userAgent;
    }

}
