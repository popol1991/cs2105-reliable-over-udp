import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Hashtable;

public class MyServerSocket implements Runnable {
	private static InetAddress destAddr;
	private DatagramSocket inSocket, outSocket;
	private int outPort;

	private PipedOutputStream internalOutputStream;
	private PipedInputStream appLayerStream;

	private Hashtable<Integer, byte[]> buffer;

	private int leftWindow = 0;

	public MyServerSocket(int inPort, int outPort) throws IOException {
		inSocket = new DatagramSocket(inPort);
		outSocket = new DatagramSocket();
		destAddr = InetAddress.getByName("127.0.0.1");
		this.outPort = outPort;

		buffer = new Hashtable<Integer, byte[]>();

		internalOutputStream = new PipedOutputStream();
		try {
			appLayerStream = new PipedInputStream(internalOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		(new Thread(this)).start();
	}

	public InputStream getInputStream() {
		return appLayerStream;
	}

	@Override
	public void run() {
		while (true) {
			int maxSize = ReliableDataPacket.PAYLOAD_SIZE;
			DatagramPacket pkt = new DatagramPacket(new byte[maxSize], maxSize);
			ReliableDataPacket receivedPkt;
			try {
				inSocket.receive(pkt);
				receivedPkt = new ReliableDataPacket(pkt, pkt.getLength());
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			int seqNo = receivedPkt.getSeqNo();

			boolean isNew = seqNo >= leftWindow && !buffer.contains(seqNo);
			if (isNew) {
				byte[] data = receivedPkt.getData();
				int index;
				for (index=0;index<data.length;index++) {
					if (data[index] == -1) {
						break;
					}
				}
				byte[] realData = new byte[index];
				System.arraycopy(data, 0, realData, 0, index);
				buffer.put(seqNo, realData);
			}

			ReliableAckPacket ack = new ReliableAckPacket(seqNo);
			byte[] ackData = ack.getByteArray();
			DatagramPacket ackPkt = new DatagramPacket(ackData, ackData.length,
					destAddr, outPort);

			try {
				outSocket.send(ackPkt);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			byte[] data;
			while ((data = buffer.get(leftWindow)) != null) {
				try {
					internalOutputStream.write(data);
					internalOutputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				leftWindow++;
			}
		}
	}
}
