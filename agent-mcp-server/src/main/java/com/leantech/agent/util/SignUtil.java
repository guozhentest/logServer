package com.leantech.agent.util;

import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;

public class SignUtil {

    /**
     * 生成 MD5 签名
     * @param apiKey  预先约定的密钥
     * @param orgCode 机构代码
     * @param timestamp 毫秒时间戳
     * @return 签名值
     */
    public static String generateSign(String apiKey, String orgCode, long timestamp) {
        String raw = apiKey + orgCode + timestamp;
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}