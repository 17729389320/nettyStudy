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
 * Channel����ʾһ�����ӣ��������Ϊÿһ�����󣬾���һ��Channel��
   ChannelHandler�����Ĵ���ҵ�����������ڴ���ҵ������
   ChannelHandlerContext�����ڴ���ҵ�����ݡ�
   ChannelPipeline�����ڱ��洦�������Ҫ�õ���ChannelHandler��ChannelHandlerContext��
   	ע�⣺netty�еķ������󲿷����첽�ģ�
 */

public class Server {
	//����ͨ��������е��¼�������
	public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	static final int PORT = 8888;
	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(5);//����������߳� �����ڴ���������˽��տͻ�������  
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);//��������ͨ�ţ���д��  
		
		try {
			//����һ��ServerBootstrap��������Netty��һϵ�в�����������ܴ������ݵĻ����С�ȡ�
			ServerBootstrap b = new ServerBootstrap();
			 //�������߳��� 
			ChannelFuture f = b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)//ָ��NIO��ģʽ
				.childHandler(new ChannelInitializer<SocketChannel>() {//����һ������ʵ�ʴ������ݵ���ChannelInitializer�����г�ʼ����׼���������������ý��ܴ������ݵ��ַ�������ʽ�Լ�ʵ�ʴ������ݵĽӿڡ�
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pl = ch.pipeline();//�ܵ�������Զ���Ĵ�����
						//���������handler
						pl.addLast(new ServerChildHandler());
					}
				})
				.bind(PORT)//��8888�˿�
				.sync();
			
			if (f.isSuccess()) {
				System.out.println("Server�������������������˿ڣ�" + PORT);
			}
			
			f.channel().closeFuture().sync(); //����close()�����ǵ��ø÷���->ChannelFuture
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
	}
}

class ServerChildHandler extends ChannelInboundHandlerAdapter { //SimpleChannleInboundHandler Codec
	//�ͻ������ӳɹ�����ʼ����ʱ����ô˷���
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelActive()");
		//��ȡ��Ӧ��ͨ���������ͨ������
		Server.clients.add(ctx.channel());
	}
	//�ͻ���д����ʱ�����ø÷���
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ByteBuf buf = null;
		try {
			buf = (ByteBuf)msg;
			
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			System.out.println(new String(bytes));
			//����ͨ�����ÿ��ͨ��������
			Server.clients.writeAndFlush(msg);
//			
//			System.out.println(buf);
//			System.out.println(buf.refCnt());
		} finally {
			//if(buf != null && buf) ReferenceCountUtil.release(buf);
			//System.out.println(buf.refCnt());
		}
	}
	//�����쳣���ô˷���
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		Server.clients.remove(ctx);
		ctx.close();
	}
	
	
}





