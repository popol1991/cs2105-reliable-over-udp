import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class ReliableDataPacket {

	public static final int PAYLOAD_SIZE = 1000;
	private static final int META_DATA_SIZE = 6;
	public static final int DATA_SIZE = PAYLOAD_SIZE - META_DATA_SIZE;

	private short origChksum, currentChksum; // 16-bit checksum according to
												// RFC768
	// 8-bit sequence number
	// the range of sequence number must be larger than 2 times the window size
	private int seqNo;
	private byte[] data;

	public ReliableDataPacket(int seqNo, byte[] buf, int length) {
		this.seqNo = seqNo;
		this.origChksum = CheckSum.compute(buf, seqNo);
		data = new byte[DATA_SIZE];
		System.arraycopy(buf, 0, data, 0, length);
	}

	public ReliableDataPacket(DatagramPacket pkt, int length) {
		byte[] content = pkt.getData();
		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(
				content));
		try {
			this.origChksum = ds.readShort();
			this.currentChksum = CheckSum.compute(content);
			this.seqNo = ds.readInt();
			data = new byte[length - META_DATA_SIZE];
			ds.read(data, 0, length - META_DATA_SIZE);
			ds.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getByteArray() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream(META_DATA_SIZE
				+ data.length);
		DataOutputStream ds = new DataOutputStream(bs);
		try {
			ds.writeShort(origChksum);
			ds.writeInt(seqNo);
			ds.write(data);
			ds.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bs.toByteArray();
	}

	public boolean isCorrupted() {
		return origChksum != currentChksum;
	}

	public byte[] getData() {
		return data;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public short getCurrentChksum() {
		return currentChksum;
	}

	public short getOrigChksum() {
		return origChksum;
	}

}
