import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class CheckSum {

	public static short compute(byte[] data, int seqNo) {
		return (short) (compute(seqNo) ^ computeByBytes(data));
	}

	public static short compute(byte[] data) {
		short sum = 0;
		ByteArrayInputStream bs = new ByteArrayInputStream(data);
		DataInputStream ds = new DataInputStream(bs);
		try {
			ds.readShort();
			int seqNo = ds.readInt();
			byte[] content = new byte[data.length - 6];
			ds.read(content, 0, content.length);
			ds.close();
			sum = (short) (compute(seqNo) ^ computeByBytes(content));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sum;
	}

	private static short computeByBytes(byte[] data) {
		short sum = 0;
		for (int i = 0; i < data.length; i++) {
			if (i % 2 == 0) {
				sum = (short) (sum ^ ((short) ~(data[i] << 8)));
			} else {
				sum = (short) (sum ^ ~(data[i]));
			}
		}
		return sum;
	}

	public static short compute(int seqNo) {
		return (short) ((seqNo & 0x00ff) ^ (seqNo >>> 16));
	}

}
