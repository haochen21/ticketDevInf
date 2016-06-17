package ticket.server.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import ticket.server.server.TicketServer;

public class TicketResponse extends ChannelOutboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(TicketResponse.class);

	@Override
	public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
		logger.info(ByteBufUtil.hexDump((ByteBuf) obj));
		ChannelFuture future = ctx.writeAndFlush(obj);
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture f) {
				if (!f.isSuccess()) {
					logger.info(ctx.channel().attr(TicketServer.deviceId).get(), f.cause().getMessage());
					f.channel().close();
				}
			}
		});
	}
}