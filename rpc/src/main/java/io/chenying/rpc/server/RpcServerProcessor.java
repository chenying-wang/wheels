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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.context.ApplicationContext;

import io.chenying.rpc.RpcResponse;
import io.chenying.rpc.annotation.RpcService;
import io.chenying.rpc.annotation.RpcServiceReference;
import io.chenying.rpc.utils.ApplicationContextUtils;
import io.chenying.rpc.utils.JsonUtils;

public class RpcServerProcessor {

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
            Object result = this.invoke(requestCtx.getMethod(), requestCtx.getBodyNode());
            return RpcResponse.newSuccessResponse(result);
        } catch (Exception e) {
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
        Object arg = this.json.read(body, this.rpcMethods.get(rpcMethod).getParameterTypes()[0]);
        return this.rpcMethods.get(rpcMethod).invoke(this.appCtx.getBean(this.rpcBeanNames.get(rpcMethod)), arg);
    }

}
