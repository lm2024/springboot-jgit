package com.redis.jedis.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供 SpringDoc v3 到 Springfox v2 的轻量兼容层，
 * 解决前端 UI 访问 /v3/api-docs 和 /v3/api-docs/swagger-config 404 的问题。
 */
@RestController
@RequestMapping("/v3/api-docs")
public class OpenApiCompatibilityController {

    /**
     * 将 /v3/api-docs 重定向到 /v2/api-docs，便于直接访问时获得文档。
     */
    @GetMapping("")
    public ResponseEntity<Void> redirectToV2Docs() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, "/v2/api-docs");
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * 提供 swagger-config，指向 Springfox 的 /v2/api-docs。
     * 适配某些前端 UI（含 Knife4j 页面）默认请求 /v3/api-docs/swagger-config 的场景。
     */
    @GetMapping("/swagger-config")
    public Map<String, Object> swaggerConfig() {
        Map<String, Object> urlEntry = new HashMap<>();
        urlEntry.put("name", "default");
        urlEntry.put("url", "/v2/api-docs");

        Map<String, Object> body = new HashMap<>();
        body.put("configUrl", "/v3/api-docs/swagger-config");
        body.put("urls", Collections.singletonList(urlEntry));
        body.put("validatorUrl", "");
        return body;
    }
}


