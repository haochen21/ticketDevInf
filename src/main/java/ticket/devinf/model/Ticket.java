package ticket.server.model;

import java.io.UnsupportedEncodingException;

import ticket.server.helper.TicketHelper;

public class Ticket {

	private byte start;

	private short commandLength;

	private short dataLength;

	private byte[] command;

	private byte error;

	private byte[] data;

	private byte xor;

	private boolean checkPass;

	public Ticket() {

	}

	public byte getStart() {
		return start;
	}

	public void setStart(byte start) {
		this.start = start;
	}

	public short getCommandLength() {
		return commandLength;
	}

	public void setCommandLength(short commandLength) {
		this.commandLength = commandLength;
	}

	public short getDataLength() {
		return dataLength;
	}

	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
	}

	public byte[] getCommand() {
		return command;
	}

	public void setCommand(byte[] command) {
		this.command = command;
	}

	public byte getError() {
		return error;
	}

	public void setError(byte error) {
		this.error = error;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte getXor() {
		return xor;
	}

	public void setXor(byte xor) {
		this.xor = xor;
	}

	public boolean isCheckPass() {
		return checkPass;
	}

	public void setCheckPass(boolean checkPass) {
		this.checkPass = checkPass;
	}

	public String getTicketString() throws UnsupportedEncodingException {
		return "Ticket [start=" + TicketHelper.INSTANCE.byteToHex(start) + ", commandLength=" + commandLength
				+ ", dataLength=" + dataLength + ", command=" + TicketHelper.INSTANCE.byteToShort(command[0])
				+ TicketHelper.INSTANCE.byteToShort(command[1]) + ", error=" + error + ", data="
				+ (data == null ? "null" : TicketHelper.INSTANCE.bytesToString(data)) + ", xor="
				+ TicketHelper.INSTANCE.byteToHex(xor) + ", checkPass=" + checkPass + "]";
	}

}
