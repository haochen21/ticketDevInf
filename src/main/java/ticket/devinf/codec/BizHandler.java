package ticket.server.codec;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ticket.server.helper.TicketHelper;
import ticket.server.model.Ticket;
import ticket.server.server.TicketServer;

public class BizHandler extends ChannelInboundHandlerAdapter {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final static Logger logger = LoggerFactory.getLogger(BizHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
		Ticket msg = (Ticket) obj;

		short commandFirst = TicketHelper.INSTANCE.byteToShort(msg.getCommand()[0]);
		short commandSecond = TicketHelper.INSTANCE.byteToShort(msg.getCommand()[1]);

		Ticket response = new Ticket();
		response.setStart((byte) 0xFE);
		response.setCommandLength((short) 3);
		response.setCommand(msg.getCommand());
        
		if(!msg.isCheckPass()){
			//数据校验失败
			response.setError((byte) 0x05);
			response.setDataLength((short)0);
			ctx.write(response);
		}else if (commandFirst == 0 && commandSecond == 1) {
			// 设备登陆上报
			String deviceId = TicketHelper.INSTANCE.bytesToString(msg.getData());
			ctx.channel().attr(TicketServer.deviceId).set(deviceId);
			logger.info("start login,command is: " + commandFirst + commandSecond + ",deviceId is: " + deviceId);

			ctx.executor().execute(new Runnable() {
				@Override
				public void run() {
					Random random = new Random();
					try {
						TimeUnit.SECONDS.sleep(random.nextInt(3));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					// 查询设备号，如果不存在，返回 01 设备编号无效
					boolean deviceIdExist = true;
					if (deviceIdExist) {
						response.setError((byte) 0x00);
						response.setDataLength(msg.getDataLength());
						response.setData(msg.getData());
					} else {
						response.setError((byte) 0x01);
						response.setDataLength((short)0);
					}
					ctx.write(response);
				}
			});
		} else if (commandFirst == 0 && commandSecond == 2) {
			// 设备心跳
			logger.info("heart beat,deviceId is: " + ctx.channel().attr(TicketServer.deviceId).get());
			response.setDataLength((short)0);
			response.setError((byte) 0x00);
			ctx.write(response);
		} else if (commandFirst == 0 && commandSecond == 3) {
			// 刷卡 数据上报
			String cardId = TicketHelper.INSTANCE.bytesToString(msg.getData());
			logger.info("send card,deviceId is: " + ctx.channel().attr(TicketServer.deviceId).get() + ",cardId is: "
					+ cardId);
			ctx.executor().execute(new Runnable() {
				@Override
				public void run() {
					Random random = new Random();
					try {
						TimeUnit.SECONDS.sleep(random.nextInt(3));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					// 如果卡号不存在或订单不存在，返回出错数据
					boolean hasOrder = true;
					if (hasOrder) {
						StringBuilder orderSb = new StringBuilder();
						orderSb.append("订单编号:111213118876");
						orderSb.append(" 提货时间:" + LocalDateTime.now().format(formatter));
						byte[] dataBytes = null;
						try {
							dataBytes = TicketHelper.INSTANCE.stringToBytes(orderSb.toString());
							response.setError((byte) 0x00);
							response.setDataLength((short) dataBytes.length);
							response.setData(dataBytes);
						} catch (Exception ex) {
							logger.info("create string to byte fail," + orderSb.toString(), ex);
							response.setDataLength((short)0);
							response.setError((byte) 0x02);
						}
					} else {
						response.setError((byte) 0x02);
						response.setDataLength((short)0);
					}
					ctx.write(response);
				}
			});
		} else if (commandFirst == 0 && commandSecond == 4) {
			// 订单完成
			String orderId = TicketHelper.INSTANCE.bytesToString(msg.getData());
			logger.info("send order,deviceId is: " + ctx.channel().attr(TicketServer.deviceId).get() + ",orderId is: "
					+ orderId);
			ctx.executor().execute(new Runnable() {
				@Override
				public void run() {
					Random random = new Random();
					try {
						TimeUnit.SECONDS.sleep(random.nextInt(3));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				    
					response.setDataLength((short)0);					
					boolean hasFinished = true;
					if (hasFinished) {
						response.setError((byte) 0x00);
					} else {
						response.setError((byte) 0x03);						
					}
					ctx.write(response);
				}
			});

		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

}
