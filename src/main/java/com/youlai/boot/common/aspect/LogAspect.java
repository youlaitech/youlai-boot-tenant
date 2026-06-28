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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * 日志切面
 *
 * @author Ray.Hao
 * @since 2020/11/06
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {
    private final LogService logService;
    private final HttpServletRequest request;
    private final CacheManager cacheManager;

    @Qualifier("operationLogExecutor")
    private final Executor operationLogExecutor;

    @Pointcut("@annotation(com.youlai.boot.common.annotation.Log)")
    public void logPointcut() {
    }

    @Around("logPointcut() && @annotation(logAnno)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log logAnno) throws Throwable {
        TimeInterval timer = DateUtil.timer();
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        String ipAddr = IPUtils.getIpAddr(request);
        String region = StrUtil.isNotBlank(ipAddr) ? IPUtils.getRegion(ipAddr) : null;
        String userAgentHeader = request.getHeader("User-Agent");

        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long executionTime = timer.interval();
            if (userId == null) {
                userId = SecurityUtils.getUserId();
                username = SecurityUtils.getUsername();
            }
            final Long finalUserId = userId;
            final String finalUsername = username;
            final Exception finalException = exception;
            final long finalExecutionTime = executionTime;
            try {
                operationLogExecutor.execute(() -> saveLog(logAnno, finalException, finalExecutionTime,
                        finalUserId, finalUsername, requestUri, requestMethod, ipAddr, region, userAgentHeader));
            } catch (Exception e) {
                log.error("提交操作日志异步任务失败", e);
            }
        }
        return result;
    }

    private void saveLog(Log logAnno, Exception exception, long executionTime, Long userId, String username,
                         String requestUri, String requestMethod, String ipAddr, String region, String userAgentHeader) {
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
        sysLog.setRequestUri(requestUri);
        sysLog.setRequestMethod(requestMethod);

        if (StrUtil.isNotBlank(ipAddr)) {
            sysLog.setIp(ipAddr);
            if (StrUtil.isNotBlank(region)) {
                String[] regionArray = region.split("\\|");
                if (regionArray.length > 3) {
                    sysLog.setProvince(regionArray[2]);
                    sysLog.setCity(regionArray[3]);
                }
            }
        }

        UserAgent userAgent = resolveUserAgent(userAgentHeader);
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

    private UserAgent resolveUserAgent(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return null;
        }
        String userAgentHash = DigestUtil.md5Hex(userAgentString);
        UserAgent userAgent = Objects.requireNonNull(cacheManager.getCache("userAgent")).get(userAgentHash, UserAgent.class);
        if (userAgent != null) {
            return userAgent;
        }
        userAgent = UserAgentUtil.parse(userAgentString);
        Objects.requireNonNull(cacheManager.getCache("userAgent")).put(userAgentHash, userAgent);
        return userAgent;
    }

}
