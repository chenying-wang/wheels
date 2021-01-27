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

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.chenying.rpc.RpcCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GenericFutureListener;

public class RpcServer {

    private final static Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private InetSocketAddress localAddress;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ExecutorService executorService;
    private Channel ch;

    private int maxLength = 65536;
    private int lengthFieldLength = 2;

    private RpcServerProcessor serverProcessor;
    private RpcCodec codec;

    public RpcServer(String host, int port) {
        this.localAddress = new InetSocketAddress(host, port);
    }

    public synchronized RpcServer init() {
        if (this.ch != null) {
            return this;
        }
        this.bossGroup = new EpollEventLoopGroup(1);
        this.workerGroup = new EpollEventLoopGroup();
        this.executorService = Executors.newFixedThreadPool(4);
        this.serverProcessor = RpcServerProcessor.create();
        this.codec = new RpcCodec();
        this.registerShutdownHook();
        return this;
    }

    public synchronized RpcServer start() {
        if (this.ch != null) {
            return this;
        }
        try {
            ServerBootstrap b = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(EpollServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                            .addLast("FrameDecoder", new LengthFieldBasedFrameDecoder(maxLength, 0, lengthFieldLength, 0, lengthFieldLength))
                            .addLast("StrDecoder", new StringDecoder())
                            .addLast("FrameEncoder", new LengthFieldPrepender(lengthFieldLength))
                            .addLast("StrEncoder", new StringEncoder())
                            .addLast("Handler", new RpcServerHandler(RpcServer.this.serverProcessor,
                                RpcServer.this.codec, RpcServer.this.executorService));
                    }
                })
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .localAddress(this.localAddress);
            b.bind().addListener((GenericFutureListener<ChannelFuture>) f -> {
                this.ch = f.channel();
                logger.info("Server started at {}", this.ch.localAddress());
            });
        } catch (Exception e) {
            logger.error("Failed to start server", e);
            this.workerGroup.shutdownGracefully();
            this.bossGroup.shutdownGracefully();
            this.ch = null;
        }
        return this;
    }

    public synchronized RpcServer stop() {
        if (this.ch == null) {
            throw new RuntimeException("Server has not started yet");
        }
        try {
            this.ch.close().addListener(future -> {
                this.workerGroup.shutdownGracefully();
                this.bossGroup.shutdownGracefully();
                this.ch = null;
            }).sync();
            logger.info("Server stopped");
        } catch (Exception e) {
            logger.error("Failed to stop Server", e);
        }
        return this;
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

}
