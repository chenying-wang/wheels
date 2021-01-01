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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.chenying.rpc.RpcCodec;
import io.chenying.rpc.RpcRequest;
import io.chenying.rpc.RpcResponse;
import io.chenying.rpc.utils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

public class RpcClientHandler extends SimpleChannelInboundHandler<String> {

    private final static Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    private JsonUtils json = JsonUtils.instance();

    private RpcCodec codec;

    private long requestId;
    private Map<Long, Consumer<RpcResponseContext>> callbacks;

    private ChannelHandlerContext ctx;

    public RpcClientHandler(RpcCodec codec) {
        this.requestId = RandomUtils.nextLong();
        this.codec = codec;
        this.callbacks = new ConcurrentHashMap<>();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelActive(ctx);
    }

    public <T, R> void send(RpcRequest<R> msg, CompletableFuture<RpcResponse<T>> future, Class<T> clazz) {
        long id = this.nextRequestId();
        msg.setId(id);
        this.ctx.write(this.codec.encode(msg));
        this.callbacks.put(id, responseCtx -> {
            RpcResponse<T> response = new RpcResponse<>();
            response.setId(responseCtx.getId());
            response.setCode(responseCtx.getCode());
            response.setMessage(responseCtx.getMessage());
            response.setBody(RpcClientHandler.this.json.read(responseCtx.getBodyNode(), clazz));
            future.complete(response);
        });
        this.ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        logger.info("Client Recv: {}", msg);
        RpcResponseContext responseCtx = this.codec.decode(msg, new TypeReference<RpcResponseContext>() {});
        long id = responseCtx.getId();
        this.callbacks.get(id).accept(responseCtx);
        this.callbacks.remove(id);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("RPC Client Error", cause);
        super.exceptionCaught(ctx, cause);
    }

    private long nextRequestId() {
        if (this.requestId == Long.MAX_VALUE) {
            this.requestId = Long.MIN_VALUE;
            return this.requestId;
        }
        return ++this.requestId;
    }

}
