package com.leantech.agent.service;

import com.leantech.agent.entity.AgentUserEntity;
import com.leantech.agent.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean validateUser(String username, String rawPassword) {
        AgentUserEntity user = userRepository.findByUsername(username);
        if (user == null || user.getPassword() == null || user.getPassword().isBlank()) {
            return false;
        }
        log.info("Validating user {}", passwordEncoder.encode(rawPassword));

        // 1. 去除首尾不可见字符（空格、制表符、换行等）
        String storedPassword = user.getPassword().trim();

        // 2. 若已是 BCrypt 密文，直接匹配
        if (storedPassword.startsWith("$2a$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        // 3. 不是 BCrypt，说明是旧版明文密码，进行明文对比，成功后自动加密
        if (rawPassword.equals(storedPassword)) {
            // 自动升级为 BCrypt
            String encoded = passwordEncoder.encode(rawPassword);
            userRepository.updatePassword(username, encoded);
            log.info("用户 [{}] 的密码已从明文自动升级为 BCrypt", username);
            return true;
        }

        return false;
    }

    public AgentUserEntity getUser(String username) {
        return userRepository.findByUsername(username);
    }
}