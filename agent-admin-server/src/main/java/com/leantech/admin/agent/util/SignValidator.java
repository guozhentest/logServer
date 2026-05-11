package com.leantech.admin.agent.util;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

public class SignValidator {

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

        if (Math.abs(System.currentTimeMillis() - timestamp) > 5 * 60 * 1000) {
            return false;
        }

        String raw = apiKey + orgCode + timestamp;
        String expected = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        return expected.equalsIgnoreCase(sign);
    }
}
