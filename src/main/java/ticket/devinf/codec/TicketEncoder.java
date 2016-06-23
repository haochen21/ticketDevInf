package ticket.server.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import ticket.server.model.Ticket;

public class TicketEncoder extends ChannelOutboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(TicketEncoder.class);

	@Override
	public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
		Ticket ticket = (Ticket) obj;

		logger.info(ticket.getTicketString());

		ByteBuf byteBuf = Unpooled.buffer();

		byteBuf.writeByte(ticket.getStart());
		byteBuf.writeByte(new Short(ticket.getCommandLength()).byteValue());
		byteBuf.writeByte(new Short(ticket.getDataLength()).byteValue());
		byteBuf.writeBytes(ticket.getCommand());
		byteBuf.writeByte(ticket.getError());
		if (ticket.getData() != null) {
			byteBuf.writeBytes(ticket.getData());
		}

		// º∆À„“ÏªÚ÷µ
		byte[] rawData = new byte[byteBuf.readableBytes()];
		byteBuf.getBytes(0, rawData);
		byte xor = (byte) (0xff & rawData[0] ^ rawData[1]);
		for (int i = 2; i < rawData.length; i++) {
			xor = (byte) (0xff & rawData[i] ^ xor);
		}
		byteBuf.writeByte(xor);

		ctx.write(byteBuf);
	}

}
