package com.youlai.boot.auth.qrcode.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.youlai.boot.auth.qrcode.model.QrCodeLoginContext;
import com.youlai.boot.auth.qrcode.model.QrCodeLoginStatusEnum;
import com.youlai.boot.auth.qrcode.model.vo.QrCodeGenerateVO;
import com.youlai.boot.auth.qrcode.model.vo.QrCodeStatusVO;
import com.youlai.boot.auth.qrcode.service.QrCodeLoginService;
import com.youlai.boot.auth.qrcode.util.QrCodeNicknameMasker;
import com.youlai.boot.common.constant.RedisConstants;
import com.youlai.boot.common.result.ResultCode;
import com.youlai.boot.framework.security.exception.TokenInvalidException;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import com.youlai.boot.framework.security.model.SecurityUser;
import com.youlai.boot.framework.security.model.SecurityUserDetails;
import com.youlai.boot.framework.security.token.TokenManager;
import com.youlai.boot.framework.tenant.TenantContextHolder;
import com.youlai.boot.system.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.TimeUnit;

/**
 * 扫码登录服务：签发二维码票据、轮询/确认状态、PC 端换取登录令牌。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeLoginServiceImpl implements QrCodeLoginService {

    /** 票据有效期（秒），到期由 Redis TTL 自动清理 */
    private static final int DEFAULT_EXPIRE_SECONDS = 300;

    /** 状态流转时剩余 TTL 低于此值则补足 */
    private static final int MIN_REMAIN_SECONDS = 30;

    /** Redis 读回的 Map 转回强类型上下文对象 */
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenManager tokenManager;
    private final UserSocialService userSocialService;

    /**
     * 生成二维码票据，初始状态 WAITING，写入 Redis 并返回 ticket 与有效期
     */
    @Override
    public QrCodeGenerateVO generate(String clientIp) {
        String ticket = IdUtil.fastSimpleUUID();
        QrCodeLoginContext ctx = new QrCodeLoginContext();
        ctx.setTicket(ticket);
        ctx.setStatus(QrCodeLoginStatusEnum.WAITING.name());
        ctx.setCreatedAt(System.currentTimeMillis());
        ctx.setClientIp(clientIp);
        save(ctx, DEFAULT_EXPIRE_SECONDS);
        return QrCodeGenerateVO.builder()
                .ticket(ticket)
                .expireSeconds(DEFAULT_EXPIRE_SECONDS)
                .build();
    }

    /**
     * 查询票据当前状态与剩余有效期，供 PC 端轮询
     */
    @Override
    public QrCodeStatusVO status(String ticket) {
        QrCodeLoginContext ctx = loadOrThrow(ticket);
        return toVO(ctx, remainingSeconds(ticket));
    }

    /**
     * APP 扫码：校验 WAITING 后写入用户信息并推进到 SCANNED
     */
    @Override
    public QrCodeStatusVO scan(String ticket, Long userId) {
        QrCodeLoginContext ctx = loadOrThrow(ticket);
        requireStatus(ctx, QrCodeLoginStatusEnum.WAITING);
        fillUserInfo(ctx, userId);
        ctx.setStatus(QrCodeLoginStatusEnum.SCANNED.name());
        ctx.setScannedAt(System.currentTimeMillis());
        save(ctx, refreshTtl(ticket));
        return toVO(ctx, remainingSeconds(ticket));
    }

    /**
     * APP 确认登录：校验 SCANNED 且为同一用户，推进到 CONFIRMED
     */
    @Override
    public QrCodeStatusVO confirm(String ticket, Long userId) {
        QrCodeLoginContext ctx = loadOrThrow(ticket);
        requireStatus(ctx, QrCodeLoginStatusEnum.SCANNED);
        requireSameUser(ctx, userId);
        ctx.setStatus(QrCodeLoginStatusEnum.CONFIRMED.name());
        ctx.setConfirmedAt(System.currentTimeMillis());
        save(ctx, refreshTtl(ticket));
        return toVO(ctx, remainingSeconds(ticket));
    }

    /**
     * 取消票据：仅流程中状态可取消，已扫码后仅扫码本人可取消
     */
    @Override
    public QrCodeStatusVO cancel(String ticket, Long userId) {
        QrCodeLoginContext ctx = loadOrThrow(ticket);
        QrCodeLoginStatusEnum current = QrCodeLoginStatusEnum.valueOf(ctx.getStatus());
        // 只有还在流程中的票据（等待扫码 / 已扫码 / 已确认）允许取消，已登录或已取消的重复操作直接拒绝
        if (current != QrCodeLoginStatusEnum.WAITING
                && current != QrCodeLoginStatusEnum.SCANNED
                && current != QrCodeLoginStatusEnum.CONFIRMED) {
            throw new TokenInvalidException(ResultCode.QR_CODE_STATUS_ILLEGAL);
        }
        // 一旦有人扫过码，取消权就归扫码本人，防止他人替扫码用户取消
        if (current != QrCodeLoginStatusEnum.WAITING && ctx.getUserId() != null) {
            requireSameUser(ctx, userId);
        }
        ctx.setStatus(QrCodeLoginStatusEnum.CANCELED.name());
        save(ctx, refreshTtl(ticket));
        return toVO(ctx, remainingSeconds(ticket));
    }

    /**
     * PC 端换取登录令牌：校验 CONFIRMED 后生成与账号密码登录同套令牌
     */
    @Override
    public AuthenticationToken login(String ticket) {
        QrCodeLoginContext ctx = loadOrThrow(ticket);
        requireStatus(ctx, QrCodeLoginStatusEnum.CONFIRMED);
        SecurityUser securityUser = getAuthInfoByUserIdIgnoreTenant(ctx.getUserId());
        if (securityUser == null) {
            throw new TokenInvalidException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        SecurityUserDetails details = new SecurityUserDetails(securityUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                details, null, details.getAuthorities());
        AuthenticationToken token = tokenManager.generateToken(authentication);
        // 换取令牌成功后立即把票据置为已使用（一次性），再次 login 会在 requireStatus(CONFIRMED) 处被拒，杜绝重放
        ctx.setStatus(QrCodeLoginStatusEnum.LOGGED_IN.name());
        save(ctx, Math.max(remainingSeconds(ticket), MIN_REMAIN_SECONDS));
        return token;
    }

    // ======================== private ========================

    /** 读取票据上下文，票据为空、不存在或已过期都视为 QR_CODE_NOT_FOUND */
    private QrCodeLoginContext loadOrThrow(String ticket) {
        if (StrUtil.isBlank(ticket)) {
            throw new TokenInvalidException(ResultCode.QR_CODE_NOT_FOUND);
        }
        Object raw = redisTemplate.opsForValue().get(key(ticket));
        if (raw == null) {
            throw new TokenInvalidException(ResultCode.QR_CODE_NOT_FOUND);
        }
        if (raw instanceof QrCodeLoginContext ctx) {
            return ctx;
        }
        // 序列化器以 Object.class 存储、未带类型信息，读回来是 Map，这里再转回强类型
        try {
            return JSON_MAPPER.convertValue(raw, QrCodeLoginContext.class);
        } catch (Exception e) {
            log.error("扫码登录上下文反序列化失败, ticket={}", ticket, e);
            throw new TokenInvalidException(ResultCode.QR_CODE_NOT_FOUND);
        }
    }

    /** 当前状态必须等于期望状态，否则说明流程被跳步或重复操作 */
    private void requireStatus(QrCodeLoginContext ctx, QrCodeLoginStatusEnum expected) {
        if (!expected.name().equals(ctx.getStatus())) {
            throw new TokenInvalidException(ResultCode.QR_CODE_STATUS_ILLEGAL);
        }
    }

    /** 操作者必须是当初扫码的那个用户，防止 A 扫码 B 确认 */
    private void requireSameUser(QrCodeLoginContext ctx, Long userId) {
        if (ctx.getUserId() == null || !ctx.getUserId().equals(userId)) {
            throw new TokenInvalidException(ResultCode.QR_CODE_USER_MISMATCH);
        }
    }

    /** 扫码时把当前 APP 用户的昵称、头像写进上下文，供 PC 端 status 展示 */
    private void fillUserInfo(QrCodeLoginContext ctx, Long userId) {
        SecurityUser info = getAuthInfoByUserIdIgnoreTenant(userId);
        if (info == null) {
            throw new TokenInvalidException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        ctx.setUserId(userId);
        ctx.setNickname(info.getNickname());
        ctx.setAvatar(info.getAvatar());
    }

    /**
     * 按用户 ID 取认证信息，跳过多租户过滤（userId 来自票据，不受 PC 端租户上下文影响）
     */
    private SecurityUser getAuthInfoByUserIdIgnoreTenant(Long userId) {
        boolean oldIgnore = TenantContextHolder.isIgnoreTenant();
        try {
            TenantContextHolder.setIgnoreTenant(true);
            return userSocialService.getAuthInfoByUserId(userId);
        } finally {
            TenantContextHolder.setIgnoreTenant(oldIgnore);
        }
    }

    /** 重写整个上下文并刷新 TTL */
    private void save(QrCodeLoginContext ctx, int ttl) {
        redisTemplate.opsForValue().set(key(ctx.getTicket()), ctx, ttl, TimeUnit.SECONDS);
    }

    /** 票据在 Redis 中的剩余秒数，取不到或已过期返回 0 */
    private int remainingSeconds(String ticket) {
        Long ttl = redisTemplate.getExpire(key(ticket), TimeUnit.SECONDS);
        return ttl == null ? 0 : Math.max(ttl.intValue(), 0);
    }

    /** 状态流转时写回的 TTL：维持剩余时间，不足 MIN_REMAIN_SECONDS 则补足 */
    private int refreshTtl(String ticket) {
        int remain = remainingSeconds(ticket);
        return remain < MIN_REMAIN_SECONDS ? MIN_REMAIN_SECONDS : remain;
    }

    /** 上下文转前端 VO，昵称脱敏、用户信息仅在扫码后暴露 */
    private QrCodeStatusVO toVO(QrCodeLoginContext ctx, int expireSeconds) {
        QrCodeLoginStatusEnum status = QrCodeLoginStatusEnum.valueOf(ctx.getStatus());
        String nickname = null;
        String avatar = null;
        // WAITING 阶段谁都能查状态，此时不能泄露用户信息；扫码/确认后才回传脱敏昵称与头像
        if (status == QrCodeLoginStatusEnum.SCANNED
                || status == QrCodeLoginStatusEnum.CONFIRMED) {
            nickname = QrCodeNicknameMasker.mask(ctx.getNickname());
            avatar = ctx.getAvatar();
        }
        return QrCodeStatusVO.builder()
                .ticket(ctx.getTicket())
                .status(status.name())
                .nickname(nickname)
                .avatar(avatar)
                .expireSeconds(expireSeconds)
                .build();
    }

    /** 拼接票据在 Redis 中的完整 Key */
    private String key(String ticket) {
        return StrUtil.format(RedisConstants.Auth.QR_CODE_LOGIN, ticket);
    }
}