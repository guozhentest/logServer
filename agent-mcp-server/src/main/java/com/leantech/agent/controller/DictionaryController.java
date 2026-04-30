package com.leantech.agent.controller;

import com.leantech.agent.model.ApiResult;
import com.leantech.agent.service.DictionaryCache;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent/dict")
public class DictionaryController extends BaseController {

    private final DictionaryCache cache;

    public DictionaryController(DictionaryCache cache) {
        this.cache = cache;
    }

    @GetMapping("/biz-types")
    public ApiResult<List<Map<String, String>>> getBizTypes() {
        List<Map<String, String>> list = cache.getBizTypes().stream()
                .map(b -> Map.of("code", b.code(), "name", b.name()))
                .collect(Collectors.toList());
        return ok(list);
    }

    @GetMapping("/service-types")
    public ApiResult<List<Map<String, String>>> getServiceTypes() {
        List<Map<String, String>> list = cache.getServiceTypes().stream()
                .map(s -> Map.of("code", s.code(), "name", s.name()))
                .collect(Collectors.toList());
        return ok(list);
    }
}