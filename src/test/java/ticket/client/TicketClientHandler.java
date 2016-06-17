package ticket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TicketClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private final static Logger logger = LoggerFactory.getLogger(TicketClientHandler.class);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		logger.info("receive: " + ByteBufUtil.hexDump(msg));
		
		byte[] datas = new byte[msg.readableBytes()];
		msg.readBytes(datas);
		
		System.out.println("Client received: " + new String(datas, "gbk"));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channle is in acitve!");
	}
	
}
