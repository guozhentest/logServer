package com.leantech.agent.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 设置超时等（已有代码保持不变）
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        restTemplate.setRequestFactory(factory);

        // ✅ 替换默认的 Jackson 转换器，添加 JavaTimeModule
        restTemplate.getMessageConverters().removeIf(
                converter -> converter instanceof MappingJackson2HttpMessageConverter
        );
        restTemplate.getMessageConverters().addFirst(new MappingJackson2HttpMessageConverter(
                Jackson2ObjectMapperBuilder.json()
                        .modules(new JavaTimeModule())
                        .build()
        ));

        return restTemplate;
    }
}