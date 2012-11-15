import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Hashtable;

public class MyServerSocket implements Runnable {
	private DatagramSocket inSocket, outSocket;
	private int outPort;

	private PipedOutputStream internalOutputStream;
	private PipedInputStream appLayerStream;

	private Hashtable<Integer, byte[]> buffer;

	private int leftWindow = 0;

	public MyServerSocket(int inPort, int outPort) throws SocketException {
		inSocket = new DatagramSocket(inPort);
		outSocket = new DatagramSocket();

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
				buffer.put(seqNo, receivedPkt.getData());
			}

			ReliableAckPacket ack = new ReliableAckPacket(seqNo);
			byte[] ackData = ack.getData();
			DatagramPacket ackPkt = new DatagramPacket(ackData, ackData.length,
					pkt.getAddress(), outPort);

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
