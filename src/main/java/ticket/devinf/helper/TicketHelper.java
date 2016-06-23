package ticket.devinf.helper;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBufUtil;

public enum TicketHelper {

	INSTANCE;

	private String CHARCODE = "gbk";

	public String byteToHex(byte value) {
		return ByteBufUtil.hexDump(new byte[] { value }).toUpperCase();
	}

	public String bytesToString(byte[] value) throws UnsupportedEncodingException {
		return new String(value, CHARCODE);
	}

	public short byteToShort(byte value) {
		return (short) value;
	}

	public byte[] stringToBytes(String value) throws UnsupportedEncodingException {
		return value.getBytes(CHARCODE);
	}

}
