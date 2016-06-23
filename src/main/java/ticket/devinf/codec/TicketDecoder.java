package ticket.devinf.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;
import ticket.devinf.helper.TicketHelper;
import ticket.devinf.model.Ticket;
import ticket.devinf.server.TicketServer;

public class TicketDecoder extends ByteToMessageDecoder {

	private static final int MAX_FRAME_SIZE = 512;

	private final static Logger logger = LoggerFactory.getLogger(TicketDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int readable = in.readableBytes();
		if (readable > MAX_FRAME_SIZE) {
			in.skipBytes(readable);
			throw new TooLongFrameException("Frame too big!");
		}

		// 包头长度3：起始位+命令长度+数据长度
		// 命令长度3：命令标号+错误标记
		// 如果buffer中的可读字节大于6个
		if (readable > 6) {
			// 标记，指向当前指针位置，读取数据时使用
			in.markReaderIndex();

			byte start = in.getByte(0);

			if (start != (byte) 0xFC) {
				logger.info("start marker is error,value is: " + TicketHelper.INSTANCE.byteToHex(start));
				in.skipBytes(readable);
				throw new CorruptedFrameException("start marker is error!");
			}
			short commandLength = in.getUnsignedByte(1);
			short dataLength = in.getUnsignedByte(2);

			byte[] commandBytes = new byte[2];
			commandBytes[0] = in.getByte(3);
			commandBytes[1] = in.getByte(4);

			byte error = in.getByte(5);

			if (in.readableBytes() < dataLength + 1) {
				// 重置标记
				in.resetReaderIndex();
				// 返回，表示等待
				return;
			}

			// 对数据进行处理
			byte[] dataBytes = new byte[dataLength];
			in.getBytes(6, dataBytes);

			// 校验异或值
			byte rawCheckValue = in.getByte(6 + dataLength);

			// 设备发送原始数据
			byte[] rawData = new byte[7 + dataLength];
			in.readBytes(rawData);
			logger.info(ByteBufUtil.hexDump(rawData));

			byte checkValue = (byte) (0xff & rawData[0] ^ rawData[1]);
			for (int i = 2; i < rawData.length - 1; i++) {
				checkValue = (byte) (0xff & rawData[i] ^ checkValue);
			}

			Ticket ticket = new Ticket();
			ticket.setStart(start);
			ticket.setCommandLength(commandLength);
			ticket.setDataLength(dataLength);
			ticket.setCommand(commandBytes);
			ticket.setError(error);
			ticket.setData(dataBytes);
			ticket.setXor(rawCheckValue);
			ticket.setCheckPass(true);
			out.add(ticket);

			if (rawCheckValue != checkValue) {
				logger.info("check value is wrong,raw value is:" + TicketHelper.INSTANCE.byteToHex(rawCheckValue)
						+ ",count value is:" + TicketHelper.INSTANCE.byteToHex(checkValue));
				ticket.setCheckPass(false);
			}
			logger.info(ticket.getTicketString());
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.info(ctx.channel().attr(TicketServer.deviceId).get() + ":" + cause.getMessage());
		ctx.close();
	}
}
