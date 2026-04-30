package com.leantech.agent.config;

import com.leantech.agent.service.DictionaryCache;
import com.leantech.agent.tool.LogQueryTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Configuration
public class AiConfig {

    private final ResourceLoader resourceLoader;

    public AiConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, LogQueryTool logQueryTool,
                                 DictionaryCache dictionaryCache) {
        String systemPrompt = buildSystemPrompt(dictionaryCache);
        return builder
                .defaultSystem(systemPrompt)
                .defaultTools(logQueryTool)
                .build();
    }

    private String buildSystemPrompt(DictionaryCache dictCache) {
        String template = loadPromptFile("classpath:prompts/system-prompt.md");
        String bizTypesDesc = dictCache.getBizTypes().stream()
                .map(bt -> bt.code() + "->" + bt.name())
                .collect(Collectors.joining("，"));
        String svcTypesDesc = dictCache.getServiceTypes().stream()
                .map(st -> st.code())
                .collect(Collectors.joining("、"));
        return template
                .replace("{BIZ_TYPES}", bizTypesDesc)
                .replace("{SERVICE_TYPES}", svcTypesDesc);
    }

    private String loadPromptFile(String path) {
        try {
            Resource resource = resourceLoader.getResource(path);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("无法加载提示词文件: " + path, e);
        }
    }
}