package com.leantech.agent.util;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

public class SignValidator {

    /**
     * 验证签名
     * @param apiKey   参与签名的密钥
     * @param orgCode  机构代码
     * @param sign     请求头中的签名
     * @param timestampStr 请求头中的时间戳（毫秒）
     * @return 验证成功返回 true，失败返回 false
     */
    public static boolean validate(String apiKey, String orgCode, String sign, String timestampStr) {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(sign) || !StringUtils.hasText(timestampStr)) {
            return false;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            return false;
        }

        // 时间戳有效期 5 分钟
        if (Math.abs(System.currentTimeMillis() - timestamp) > 5 * 60 * 1000) {
            return false;
        }

        String raw = apiKey + orgCode + timestamp;
        String expected = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        return expected.equalsIgnoreCase(sign);
    }
}