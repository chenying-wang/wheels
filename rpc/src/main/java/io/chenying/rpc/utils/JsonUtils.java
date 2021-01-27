/**
 * Copyright 2020 Chenying Wang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chenying.rpc.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonUtils {

    private final static Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private final static String EMPTY_JSON = "{}";
    private final static byte[] EMPTY_JSON_BYTES = JsonUtils.EMPTY_JSON.getBytes(StandardCharsets.UTF_8);

    private static JsonUtils instance = new JsonUtils();

    private static JsonMapper mapper = JsonMapper.builder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build();

    private JsonUtils() {}

    public static JsonUtils instance() {
        return JsonUtils.instance;
    }

    public static JsonMapper mapper() {
        return JsonUtils.mapper;
    }

    public <T> T read(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JsonUtils.mapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("JSON Deserialization Error", e);
        }
        return null;
    }

    public <T> T read(String json, TypeReference<T> typeRef) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JsonUtils.mapper.readValue(json, typeRef);
        } catch (Exception e) {
            logger.error("JSON Deserialization Error", e);
        }
        return null;
    }

    public <T> T read(JsonNode json, Class<T> clazz) {
        if (json == null || json.isNull()) {
            return null;
        }
        try {
            return JsonUtils.mapper.treeToValue(json, clazz);
        } catch (Exception e) {
            logger.error("JSON Deserialization Error", e);
        }
        return null;
    }

    public <T> T read(JsonNode json, TypeReference<T> typeRef) {
        if (json == null || json.isNull()) {
            return null;
        }
        try {
            return JsonUtils.mapper.convertValue(json, typeRef);
        } catch (Exception e) {
            logger.error("JSON Deserialization Error", e);
        }
        return null;
    }

    public <T> String write(T obj) {
        if (obj == null) {
            return JsonUtils.EMPTY_JSON;
        } else if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return JsonUtils.mapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("JSON Serialization Error", e);
        }
        return JsonUtils.EMPTY_JSON;
    }

    public String write(JsonNode json) {
        if (json == null || json.isNull()) {
            return JsonUtils.EMPTY_JSON;
        }
        try {
            return JsonUtils.mapper.writeValueAsString(json);
        } catch (Exception e) {
            logger.error("JSON Serialization Error", e);
        }
        return JsonUtils.EMPTY_JSON;
    }

    public <T> byte[] writeBytes(T obj) {
        if (obj == null) {
            return JsonUtils.EMPTY_JSON_BYTES;
        } else if (obj instanceof String) {
            return ((String) obj).getBytes(StandardCharsets.UTF_8);
        }
        try {
            return JsonUtils.mapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            logger.error("JSON Serialization Error", e);
        }
        return JsonUtils.EMPTY_JSON_BYTES;
    }

    public byte[] writeBytes(JsonNode json) {
        if (json == null || json.isNull()) {
            return JsonUtils.EMPTY_JSON_BYTES;
        }
        try {
            return JsonUtils.mapper.writeValueAsBytes(json);
        } catch (Exception e) {
            logger.error("JSON Serialization Error", e);
        }
        return JsonUtils.EMPTY_JSON_BYTES;
    }

}
