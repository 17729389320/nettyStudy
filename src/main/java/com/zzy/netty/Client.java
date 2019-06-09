package com.zzy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {
	private Channel channel = null;
	
	public void send(String msg) {
		ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
		channel.writeAndFlush(buf);
	}
	

	public void connect() {
		//线程池
		EventLoopGroup group = new NioEventLoopGroup(1);
		Bootstrap b = new Bootstrap();
		try {			
			ChannelFuture f = 
					b.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ClientChannelInitializer())
				.connect("localhost", 8888)
				;
						
			f.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(!future.isSuccess()) {
						System.out.println("not connected!");
					} else {
						// initialize the channel
						channel = future.channel();
						System.out.println("connected!");
					}
				}
			});
			
			f.sync();
			
			System.out.println("...");
			
			
			f.channel().closeFuture().sync();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			group.shutdownGracefully();
		}
	}
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new ClientHandler());
	}
	
}

class ClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = null;
		try {
			buf = (ByteBuf)msg;	
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			System.out.println(new String(bytes));
			//System.out.println(buf);
			//System.out.println(buf.refCnt());
		} finally {
			if(buf != null) ReferenceCountUtil.release(buf);
			//System.out.println("引用个数:"+buf.refCnt());
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//channle 第一次连上可用，写出一个字符串 Direct Memory 
		//普通的ByteBuffer中，网络数据首先写给操作系统，java虚拟机使用时候，需要先copy到虚拟机内存中，而 ByteBuf从虚拟机直接访问操作操作系统内存 ，效率高；
		ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
		ctx.writeAndFlush(buf);//自动释放
	}
	
}
