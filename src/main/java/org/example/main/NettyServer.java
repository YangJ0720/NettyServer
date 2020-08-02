package org.example.main;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;


class NettyServer {

    public static void main(String[] args) {
        new NettyServer().start();
    }

    private void start() {
        // 1.定义server启动类
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 2.定义工作组:boss分发请求给各个worker:boss负责监听端口请求，worker负责处理请求（读写）
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();
        // 3.定义工作组
        serverBootstrap.group(parentGroup, childGroup);
        // 4.设置通道channel
        serverBootstrap.channel(NioServerSocketChannel.class);
        // 5.添加handler，管道中的处理器，通过ChannelInitializer来构造
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) throws Exception {
                // 每次客户端连接都会调用，是为通道初始化的方法，获得通道channel中的管道链（执行链、handler链）
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decode", new StringDecoder());
                pipeline.addLast("encode", new StringEncoder());
                pipeline.addLast(new IdleStateHandler(30, 30, 30));
                pipeline.addLast("handler", new ServerHandler());
            }
        });
        // 6.设置参数，TCP参数
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128); // 连接缓冲池的大小
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true); // 关闭延迟发送
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // 维持链接的活跃，清除死链接
        serverBootstrap.childOption(ChannelOption.SO_TIMEOUT, 10000);
        serverBootstrap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        // 7.绑定ip和port
        try {
            ChannelFuture channelFuture = serverBootstrap.bind(8888).sync(); // Future模式的channel对象
            System.out.println("------------------------ 服务端启动成功 ------------------------");
            // 7.5.监听关闭
            channelFuture.channel().closeFuture().sync(); // 等待服务关闭，关闭后应该释放资源
            System.out.println("------------------------ 服务端运行结束 ------------------------");
        } catch (InterruptedException e) {
            System.out.println("服务端启动失败: " + e);
            e.printStackTrace();
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }
}