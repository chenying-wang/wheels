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
package io.chenying.rpc.server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import io.chenying.rpc.RpcResponse;
import io.chenying.rpc.annotation.RpcService;
import io.chenying.rpc.annotation.RpcServiceReference;
import io.chenying.rpc.utils.ApplicationContextUtils;
import io.chenying.rpc.utils.JsonUtils;

public class RpcServerProcessor {

    private final static Logger logger = LoggerFactory.getLogger(RpcServerProcessor.class);

    private JsonUtils json = JsonUtils.instance();

    private ApplicationContext appCtx;
    private Map<String, String> rpcBeanNames;
    private Map<String, Method> rpcMethods;

    private RpcServerProcessor() {
        this.rpcBeanNames = new HashMap<>();
        this.rpcMethods = new HashMap<>();
    }

    public static RpcServerProcessor create() {
        return new RpcServerProcessor().init();
    }

    public RpcResponse<?> process(RpcRequestContext requestCtx) throws Exception {
        try {
            Object result = this.invoke(requestCtx.getMethod(), requestCtx.getParametersNode());
            return RpcResponse.newSuccessResponse(result);
        } catch (Exception e) {
            logger.error("RPC Server Invocation Error", e);
            return RpcResponse.newFailedResponse(-1, e.getMessage());
        }
    }

    private RpcServerProcessor init() {
        this.appCtx = ApplicationContextUtils.getApplicationContext();
        Map<String, Object> beans = this.appCtx.getBeansWithAnnotation(RpcServiceReference.class);
        beans.forEach((k, v) -> {
            Class<?> clazz = v.getClass();
            List<Class<?>> rpcServices = new ArrayList<>();
            for (Class<?> inf : clazz.getInterfaces()) {
                if (inf.getAnnotation(RpcService.class) == null) {
                    continue;
                }
                rpcServices.add(inf);
            }
            if (rpcServices.isEmpty()) {
                return;
            }
            for (Method method : clazz.getMethods()) {
                for (Class<?> rpcService : rpcServices) {
                    try {
                        rpcService.getMethod(method.getName(), method.getParameterTypes());
                    } catch (Exception e) {
                        continue;
                    }
                    String rpcServiceId = new StringBuilder()
                            .append(rpcService.getName())
                            .append('#')
                            .append(method.getName()).toString();
                    this.rpcBeanNames.put(rpcServiceId, k);
                    this.rpcMethods.put(rpcServiceId, method);
                }
            }
        });
        return this;
    }

    private Object invoke(String rpcMethod, JsonNode body) throws Exception {
        if (!this.rpcBeanNames.containsKey(rpcMethod) || !this.rpcMethods.containsKey(rpcMethod)) {
            throw new RuntimeException("Method Not Found");
        }

        Object instance = this.appCtx.getBean(this.rpcBeanNames.get(rpcMethod));
        Method method = this.rpcMethods.get(rpcMethod);
        int parameterCount = method.getParameterCount();

        if (parameterCount == 0) {
            return method.invoke(instance);
        } else if (parameterCount == 1 && !body.isArray()) {
            Object arg = this.json.read(body, method.getParameterTypes()[0]);
            return method.invoke(instance, arg);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        Iterator<JsonNode> it = body.elements();
        Object[] args =  new Object[parameterCount];
        for (int i = 0; i < parameterCount && it.hasNext(); ++i) {
            if (parameterTypes[i] == String.class) {
                args[i] = it.next().asText();
                continue;
            } else if (!parameterTypes[i].isPrimitive()) {
                args[i] = this.json.read(it.next(), parameterTypes[i]);
                continue;
            }

            if (parameterTypes[i] == boolean.class) {
                args[i] = it.next().asBoolean();
            } else if (parameterTypes[i] == char.class) {
                args[i] = (char) it.next().asInt();
            } else if (parameterTypes[i] == byte.class) {
                args[i] = (byte) it.next().asInt();
            } else if (parameterTypes[i] == short.class) {
                args[i] = (short) it.next().asInt();
            } else if (parameterTypes[i] == int.class) {
                args[i] = it.next().asInt();
            } else if (parameterTypes[i] == long.class) {
                args[i] = it.next().asLong();
            } else if (parameterTypes[i] == float.class) {
                args[i] = (float) it.next().asDouble();
            } else if (parameterTypes[i] == double.class) {
                args[i] = it.next().asDouble();
            } else {
                args[i] = null;
            }
        }
        return method.invoke(instance, args);
    }

}
