import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ReliableAckPacket {
	public static final int PAYLOAD_SIZE = 1000;
	public static final int META_DATA_SIZE = 6;
	public static final int DATA_SIZE = PAYLOAD_SIZE - META_DATA_SIZE;
	private int seqNo;
	private short origChksum, currentChksum;

	public ReliableAckPacket(byte[] content) {
		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(
				content));
		this.origChksum = CheckSum.compute(content);
		try {
			this.currentChksum = ds.readShort();
			this.seqNo = ds.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ReliableAckPacket(int seqNo) {
		this.seqNo = seqNo;
		this.origChksum = CheckSum.compute(seqNo);
	}

	public byte[] getByteArray() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream(3);
		DataOutputStream ds = new DataOutputStream(bs);
		try {
			ds.writeShort(origChksum);
			ds.writeInt(seqNo);
			ds.write(new byte[DATA_SIZE]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bs.toByteArray();
	}

	public int getSeqNo() {
		return seqNo;
	}

	public short getOrigChecksum() {
		return origChksum;
	}

	public short getCurrentChecksum() {
		return currentChksum;
	}

}
