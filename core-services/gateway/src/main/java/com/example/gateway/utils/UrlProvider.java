package com.example.gateway.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UrlProvider {

    private static Map<String, String> urlPostHooksMap;
    private static Map<String, String> urlPreHooksMap;
    @Autowired
    private ResourceLoader resourceLoader;
    @Value("${url.posthook.lists}")
    private String postHookUrls;
    @Value("${url.prehook.lists}")
    private String preHookUrls;

    public static Map<String, String> getUrlPostHooksMap() {
        return urlPostHooksMap;
    }

    public static Map<String, String> getUrlPreHooksMap() {
        return urlPreHooksMap;
    }

    private Map<String, String> getUrlToUrlMapping(String config) {
        String[] urlArray;
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(config))
            return Collections.unmodifiableMap(map);

        if (
                StringUtils.startsWithIgnoreCase(config, "http://")
                        || StringUtils.startsWithIgnoreCase(config, "https://")
                        || StringUtils.startsWithIgnoreCase(config, "file://")
                        || StringUtils.startsWithIgnoreCase(config, "classpath:")
        ) {
            ObjectMapper mapper = new ObjectMapper(new JsonFactory());

            Resource resource = resourceLoader.getResource(config);
            try {
                map = mapper.readValue(resource.getInputStream(), map.getClass());
            } catch (IOException e) {
                log.error("IO Exception while mapping resource: " + e.getMessage());
            }

        } else {
            urlArray = config.split("\\|");

            for (int i = 0; i < urlArray.length; i++) {

                String[] index = urlArray[i].split(":", 2);
                map.put(index[0], index[1]);
            }
            urlPostHooksMap = Collections.unmodifiableMap(map);
        }

        return Collections.unmodifiableMap(map);
    }

    @PostConstruct
    public void loadUrls() {
        urlPostHooksMap = getUrlToUrlMapping(postHookUrls);
        urlPreHooksMap = getUrlToUrlMapping(preHookUrls);

    }
}
