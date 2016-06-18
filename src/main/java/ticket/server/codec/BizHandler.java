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
			//����У��ʧ��
			response.setError((byte) 0x05);
			response.setDataLength((short)0);
			ctx.write(response);
		}else if (commandFirst == 0 && commandSecond == 1) {
			// �豸��½�ϱ�
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
					// ��ѯ�豸�ţ���������ڣ����� 01 �豸�����Ч
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
			// �豸����
			logger.info("heart beat,deviceId is: " + ctx.channel().attr(TicketServer.deviceId).get());
			response.setDataLength((short)0);
			response.setError((byte) 0x00);
			ctx.write(response);
		} else if (commandFirst == 0 && commandSecond == 3) {
			// ˢ�� �����ϱ�
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
					// ������Ų����ڻ򶩵������ڣ����س�������
					boolean hasOrder = true;
					if (hasOrder) {
						StringBuilder orderSb = new StringBuilder();
						orderSb.append("�������:111213118876");
						orderSb.append(" ���ʱ��:" + LocalDateTime.now().format(formatter));
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
			// �������
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
