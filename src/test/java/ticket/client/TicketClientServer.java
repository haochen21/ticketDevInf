package ticket.client;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TicketClientServer {

	private final String host;

	private final int port;

	private SocketChannel socketChannel;

	private final static Logger logger = LoggerFactory.getLogger(TicketClientServer.class);

	public TicketClientServer(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();

		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host, port))
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new TicketClientHandler());
					}
				});
		ChannelFuture future = bootstrap.connect(host, port).sync();
		if (future.isSuccess()) {
			socketChannel = (SocketChannel) future.channel();
			System.out.println("connect server  成功---------");
		}

	}

	public ByteBuf createLogin(String deviceId) throws Exception {
		byte[] data = deviceId.getBytes("gbk");

		ByteBuf byteBuf = Unpooled.buffer();
		byteBuf.writeByte((byte) 0xFC);
		byteBuf.writeByte((byte) 0x03);
		byteBuf.writeByte(data.length);
		byteBuf.writeBytes(new byte[] { (byte) 0x00, (byte) 0x01 });
		byteBuf.writeByte((byte) 0x00);
		byteBuf.writeBytes(data);
		// 计算异或值
		byte[] rawData = new byte[byteBuf.readableBytes()];
		byteBuf.getBytes(0, rawData);
		byte checkValue = (byte) (0xff & rawData[0] ^ rawData[1]);
		for (int i = 2; i < rawData.length; i++) {
			checkValue = (byte) (0xff & rawData[i] ^ checkValue);
		}
		byteBuf.writeByte(checkValue);
		logger.info("login: " + ByteBufUtil.hexDump(byteBuf));
		return byteBuf;
	}

	public void heartBear() {	

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						TimeUnit.SECONDS.sleep(10);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					ByteBuf byteBuf = Unpooled.buffer();
					byteBuf.writeByte((byte) 0xFC);
					byteBuf.writeByte((byte) 0x03);
					byteBuf.writeByte((byte) 0x00);
					byteBuf.writeBytes(new byte[] { (byte) 0x00, (byte) 0x02 });
					byteBuf.writeByte((byte) 0x00);
					// 计算异或值
					byte[] rawData = new byte[byteBuf.readableBytes()];
					byteBuf.getBytes(0, rawData);
					byte checkValue = (byte) (0xff & rawData[0] ^ rawData[1]);
					for (int i = 2; i < rawData.length; i++) {
						checkValue = (byte) (0xff & rawData[i] ^ checkValue);
					}
					byteBuf.writeByte(checkValue);
					
					socketChannel.writeAndFlush(byteBuf);
				}
			}
		}).start();
	}
	
	public void order(String cardId) throws Exception{	
		byte[] data = cardId.getBytes("gbk");
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						TimeUnit.SECONDS.sleep(new Random().nextInt(10));
					} catch (Exception ex) {
						ex.printStackTrace();
					}					

					ByteBuf byteBuf = Unpooled.buffer();
					byteBuf.writeByte((byte) 0xFC);
					byteBuf.writeByte((byte) 0x03);
					byteBuf.writeByte(data.length);
					byteBuf.writeBytes(new byte[] { (byte) 0x00, (byte) 0x03 });
					byteBuf.writeByte((byte) 0x00);
					byteBuf.writeBytes(data);
					// 计算异或值
					byte[] rawData = new byte[byteBuf.readableBytes()];
					byteBuf.getBytes(0, rawData);
					byte checkValue = (byte) (0xff & rawData[0] ^ rawData[1]);
					for (int i = 2; i < rawData.length; i++) {
						checkValue = (byte) (0xff & rawData[i] ^ checkValue);
					}
					byteBuf.writeByte(checkValue);
					
					socketChannel.writeAndFlush(byteBuf);
				}
			}
		}).start();
	}

	public static void main(String[] args) {
		String host = "127.0.0.1";
		int port = Integer.parseInt("9999");
		TicketClientServer server = new TicketClientServer(host, port);
		try {
			server.start();
			// login
			ByteBuf loginByteBuf = server.createLogin("device-0001");
			server.socketChannel.writeAndFlush(loginByteBuf);
			server.heartBear();
			server.order("card-1234878");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}