package com.zzy.netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
/*
 * Channel，表示一个连接，可以理解为每一个请求，就是一个Channel。
   ChannelHandler，核心处理业务就在这里，用于处理业务请求。
   ChannelHandlerContext，用于传输业务数据。
   ChannelPipeline，用于保存处理过程需要用到的ChannelHandler和ChannelHandlerContext。
   	注意：netty中的方法绝大部分是异步的；
 */

public class Server {
	//处理通道组的所有的事件，便利
	public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	static final int PORT = 8888;
	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(5);//管理请求的线程 ，用于处理服务器端接收客户端连接  
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);//进行网络通信（读写）  
		
		try {
			//创建一个ServerBootstrap对象，配置Netty的一系列参数，例如接受传出数据的缓存大小等。
			ServerBootstrap b = new ServerBootstrap();
			 //绑定两个线程组 
			ChannelFuture f = b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)//指定NIO的模式
				.childHandler(new ChannelInitializer<SocketChannel>() {//创建一个用于实际处理数据的类ChannelInitializer，进行初始化的准备工作，比如设置接受传出数据的字符集、格式以及实际处理数据的接口。
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pl = ch.pipeline();//管道，添加自定义的处理器
						//处理请求的handler
						pl.addLast(new ServerChildHandler());
					}
				})
				.bind(PORT)//绑定8888端口
				.sync();
			
			if (f.isSuccess()) {
				System.out.println("Server服务器已启动，监听端口：" + PORT);
			}
			
			f.channel().closeFuture().sync(); //调用close()方法是调用该方法->ChannelFuture
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
	}
}

class ServerChildHandler extends ChannelInboundHandlerAdapter { //SimpleChannleInboundHandler Codec
	//客户端连接成功，初始化的时候调用此方法
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelActive()");
		//获取对应的通道，添加至通道组中
		Server.clients.add(ctx.channel());
	}
	//客户端写数据时，调用该方法
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ByteBuf buf = null;
		try {
			buf = (ByteBuf)msg;
			
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			System.out.println(new String(bytes));
			//便利通道组的每个通道的数据
			Server.clients.writeAndFlush(msg);
//			
//			System.out.println(buf);
//			System.out.println(buf.refCnt());
		} finally {
			//if(buf != null && buf) ReferenceCountUtil.release(buf);
			//System.out.println(buf.refCnt());
		}
	}
	//出现异常调用此方法
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		Server.clients.remove(ctx);
		ctx.close();
	}
	
	
}





