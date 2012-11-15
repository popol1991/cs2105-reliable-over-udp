public class ReliableAckPacket {
	public static final int DATA_SIZE = 1000;
	private int seqNo;
	private short origChksum,currentChksum;
	private byte[] data;

	public ReliableAckPacket(byte[] data) {
		assert (data != null);
		this.data  = data;
		this.origChksum = CheckSum.compute(data);
		this.currentChksum = CheckSum.getChecksum(data);
		if (data.length > 2) {
			this.seqNo = (int) (data[2] & 0xff);
		} else {
			this.seqNo = -1;
		}
	}

	public ReliableAckPacket(int seqNo) {
		this.seqNo = seqNo;
		this.origChksum = CheckSum.compute(seqNo);
		this.data = constructData(origChksum,seqNo);
	}

	private byte[] constructData(short checksum, int seqNo) {
		byte[] data = new byte[3];
		data[0] = (byte) (checksum >>> 8); // logic right shift
		data[1] = (byte) (checksum & 0xff);
		data[2] = (byte) seqNo;
		return data;
	}

	public Integer getSeqNo() {
		return seqNo;
	}

	public short getChecksum() {
		return origChksum;
	}

	public byte[] getData() {
		return data;
	}

}
