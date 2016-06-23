package ticket.devinf.server;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import ticket.devinf.codec.BizHandler;
import ticket.devinf.codec.ReadTimeOutHandler;
import ticket.devinf.codec.TicketDecoder;
import ticket.devinf.codec.TicketEncoder;
import ticket.devinf.codec.TicketResponse;

@Component
public class TicketServer {

	public final static AttributeKey<String> deviceId = AttributeKey.valueOf("deviceId");

	private final static Logger logger = LoggerFactory.getLogger(TicketServer.class);

	private int port;

	private int readTimeOut;

	private int eventExecutor;

	public TicketServer(int port, int readTimeOut, int eventExecutor) {
		this.port = port;
		this.readTimeOut = readTimeOut;
		this.eventExecutor = eventExecutor;
	}

	@PostConstruct
	public void start() throws Exception {
		final EventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(eventExecutor);

		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {

							// 都属于ChannelOutboundHandler，逆序执行
							ch.pipeline().addLast(eventExecutorGroup, new TicketResponse());
							ch.pipeline().addLast(eventExecutorGroup, new TicketEncoder());

							// 都属于ChannelIntboundHandler，按照顺序执行
							ch.pipeline().addLast(new IdleStateHandler(readTimeOut, 0, 0, TimeUnit.SECONDS));
							ch.pipeline().addLast(new ReadTimeOutHandler());
							ch.pipeline().addLast(new TicketDecoder());
							ch.pipeline().addLast(eventExecutorGroup, new BizHandler());
						}
					});
			ChannelFuture f = b.bind(port).sync();
			if (f.isSuccess()) {
				logger.info("server start---------------");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
