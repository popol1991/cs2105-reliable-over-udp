import java.net.DatagramPacket;

public class ReliablePacket {
	private static final int BITS_OF_META_DATA = 3;

	private DatagramPacket packet;
	private short checksum; // 16-bit checksum follow RFC768
	private byte seqNo; // 8-bit sequence number
	private byte[] data; // real data
	private boolean isAcknowledged;

	public ReliablePacket(DatagramPacket packet) {
		byte[] data = packet.getData();
		checksum = (short) ((data[0] << 8) | (data[1] & 0xFF));
		seqNo = data[2];
		this.data = new byte[data.length - BITS_OF_META_DATA];
		for (int i = BITS_OF_META_DATA; i < data.length; i++) {
			this.data[i - BITS_OF_META_DATA] = data[i];
		}
	}

	public DatagramPacket getPacket() {
		return packet;
	}

	public void setPacket(DatagramPacket packet) {
		this.packet = packet;
	}

	public short getChecksum() {
		return checksum;
	}

	public void setChecksum(short checksum) {
		this.checksum = checksum;
	}

	public byte getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(byte seqNo) {
		this.seqNo = seqNo;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isAcknowledged() {
		return isAcknowledged;
	}

	public void setAcknowledged(boolean isAcknowledged) {
		this.isAcknowledged = isAcknowledged;
	}

}
