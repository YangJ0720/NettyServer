package org.example.main;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static List<Channel> sList = new ArrayList<>();

    public ServerHandler() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    List<Channel> list = sList;
                    for (int i = 0; i < list.size(); i++) {
                        Channel channel = list.get(i);
                        System.out.println(channel.remoteAddress() + "连接状态: " + channel.isActive());
                    }
                }
            }
        }).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead -> msg = " + msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("[客户端认证]: " + ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (IdleState.ALL_IDLE == ((IdleStateEvent) evt).state()) {
                System.out.println("移除: " + ctx.channel().remoteAddress());
                ctx.channel().close();
                sList.remove(ctx);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
        System.out.println("[连接异常]: " + cause.getMessage());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        sList.add(ctx.channel());
        System.err.println("[客户端连接]: " + ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        sList.add(ctx.channel());
        System.err.println("[客户端退出]: " + ctx.channel().remoteAddress());
    }
}
