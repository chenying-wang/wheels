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

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import io.chenying.rpc.RpcCodec;
import io.chenying.rpc.RpcRequest;
import io.chenying.rpc.RpcResponse;
import io.chenying.rpc.utils.ApplicationContextUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class RpcClient implements InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private InetSocketAddress remoteAddress;

    private EventLoopGroup workerGroup;
    private ChannelPool channelPool;

    private RpcCodec codec = new RpcCodec();

    private int maxLength = 65536;
    private int lengthFieldLength = 2;
    private int maxConnections = 3;

    private RpcClientContextInitializer contextInitializer;
    private List<Class<?>> rpcServices;

    public RpcClient(String host, int port) {
        this.remoteAddress = new InetSocketAddress(host, port);
        this.contextInitializer = new RpcClientContextInitializer(this);
        this.rpcServices = new LinkedList<>();
    }

    public RpcClient addService(Class<?> rpcService) {
        this.rpcServices.add(rpcService);
        return this;
    }

    public synchronized RpcClient init() {
        this.workerGroup = new EpollEventLoopGroup();
        this.registerShutdownHook();
        return this;
    }

    public synchronized RpcClient start() {
        if (this.channelPool != null) {
            return this;
        }
        this.channelPool = new FixedChannelPool(new Bootstrap()
                .group(this.workerGroup)
                .channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(this.remoteAddress),
            new AbstractChannelPoolHandler(){
                @Override
                public void channelCreated(Channel ch) throws Exception {
                    ch.pipeline()
                        .addLast("FrameDecoder", new LengthFieldBasedFrameDecoder(maxLength, 0, lengthFieldLength, 0, lengthFieldLength))
                        .addLast("StrDecoder", new StringDecoder())
                        .addLast("FrameEncoder", new LengthFieldPrepender(lengthFieldLength))
                        .addLast("StrEncoder", new StringEncoder())
                        .addLast("Handler", new RpcClientHandler(RpcClient.this.codec));
                }
            }, maxConnections);
        logger.info("Client started");
        return this;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
        if (appCtx instanceof GenericApplicationContext) {
            this.contextInitializer.initialize((GenericApplicationContext) appCtx, this.rpcServices);
        } else {
            throw new RuntimeException("Application Context has to be a instance of " + GenericApplicationContext.class.getName());
        }
    }

    public synchronized RpcClient stop() {
        if (this.channelPool == null) {
            return this;
        }
        this.channelPool.close();
        this.channelPool = null;
        this.workerGroup.shutdownGracefully();
        logger.info("Client stopped");
        return this;
    }

    public <T, R> CompletableFuture<RpcResponse<T>> request(RpcRequest<R> msg, Class<T> clazz) {
        CompletableFuture<RpcResponse<T>> future = new CompletableFuture<>();
        try {
            this.channelPool.acquire().addListener((GenericFutureListener<Future<Channel>>) f -> {
                Channel ch = f.getNow();
                ch.pipeline().get(RpcClientHandler.class).send(msg, future, clazz);
                RpcClient.this.channelPool.release(ch);
            });
            return future;
        } catch (Exception e) {
            logger.error("RPC Client Request Error", e);
            future.completeExceptionally(e);
        }
        return future;
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

}
