package com.leantech.admin.agent.util;

import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;

public class SignUtil {

    public static String generateSign(String apiKey, String orgCode, long timestamp) {
        String raw = apiKey + orgCode + timestamp;
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
