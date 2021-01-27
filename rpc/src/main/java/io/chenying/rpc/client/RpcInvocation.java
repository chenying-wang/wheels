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
package io.chenying.rpc.client;

import java.util.concurrent.CompletableFuture;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import io.chenying.rpc.RpcRequest;
import io.chenying.rpc.RpcResponse;

public class RpcInvocation implements MethodInterceptor {

    private Class<?> rpcService;
    private RpcClient client;

    public RpcInvocation(Class<?> rpcService, RpcClient client) {
        this.rpcService = rpcService;
        this.client = client;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        RpcRequest<Object[]> msg = new RpcRequest<>();
        String rpcServiceId = new StringBuilder().append(this.rpcService.getName()).append('#')
                .append(invocation.getMethod().getName()).toString();
        msg.setMethod(rpcServiceId);
        msg.setParameters(invocation.getArguments());
        CompletableFuture<?> response = this.client.request(msg, invocation.getMethod().getReturnType());
        return ((RpcResponse<?>) response.get()).getBody();
    }

}
