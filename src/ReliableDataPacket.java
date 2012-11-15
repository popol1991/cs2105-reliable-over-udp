import java.net.DatagramPacket;

public class ReliableDataPacket {

	public static final int PAYLOAD_SIZE = 1000;
	public static final int DATA_SIZE = 997;
	private static final int META_DATA_SIZE = 3;

	private short origChksum, currentChksum; // 16-bit checksum according to
												// RFC768
	// 8-bit sequence number
	// the range of sequence number must be larger than 2 times the window size
	private int seqNo;

	private byte[] data;

	public ReliableDataPacket(int seqNo, byte[] buf, int bytes) {
		this.seqNo = seqNo;
		this.origChksum = CheckSum.compute(buf, seqNo);
		this.data = constructData(buf, seqNo, origChksum);
	}

	public ReliableDataPacket(DatagramPacket pkt, int length) {
		this.data = pkt.getData();
		this.origChksum = CheckSum.getChecksum(data);
		this.currentChksum = CheckSum.compute(data);
		this.seqNo = (int) (data[2] & 0xff);
	}

	public byte[] getData() {
		return data;
	}

	public int getSeqNo() {
		return seqNo;
	}

	/**
	 * 
	 * the first two bytes of data store checksum value, the third byte of data
	 * stores the sequence number, and the rest of data stores the payload
	 * 
	 */
	private byte[] constructData(byte[] buf, int seqNo, short checksum) {
		byte[] data = new byte[buf.length + META_DATA_SIZE];
		data[0] = (byte) (checksum >>> 8); // logic right shift
		data[1] = (byte) (checksum & 0xff);
		data[2] = (byte) seqNo;
		for (int i = 0; i < buf.length; i++) {
			data[META_DATA_SIZE + i] = buf[i];
		}
		return data;
	}

	public short getCurrentChksum() {
		return currentChksum;
	}

}
