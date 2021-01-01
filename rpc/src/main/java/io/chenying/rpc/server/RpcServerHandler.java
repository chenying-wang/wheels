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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.chenying.rpc.RpcCodec;
import io.chenying.rpc.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<String> {

    private final static Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    private RpcServerProcessor serverProcessor;
    private RpcCodec codec;
    private ExecutorService executorService;

    public RpcServerHandler(RpcServerProcessor serverProcessor, RpcCodec codec, ExecutorService executorService) {
        this.serverProcessor = serverProcessor;
        this.codec = codec;
        this.executorService = executorService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        this.handle(msg)
            .thenAcceptAsync(ctx::writeAndFlush, ctx.executor());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("RPC Server Error", cause);
        super.exceptionCaught(ctx, cause);
    }

    private CompletableFuture<String> handle(String request) {
        CompletableFuture<String> future = new CompletableFuture<>();
        this.executorService.submit(() -> {
            RpcRequestContext requestCtx = this.codec.decode(request, new TypeReference<RpcRequestContext>() {});
            try {
                RpcResponse<?> response = this.serverProcessor.process(requestCtx);
                response.setId(requestCtx.getId());
                future.complete(this.codec.encode(response));
            } catch (Exception e) {
                logger.error("RPC Server Process Error", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

}
