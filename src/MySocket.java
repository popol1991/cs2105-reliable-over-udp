import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MySocket {
	private InetAddress destAddr;
	private int inPort, outPort;
	private DatagramSocket inSocket, outSocket;

	private Hashtable<Integer, ReliableDataPacket> buffer;
	private Set<Integer> ackedNos;

	private int nextSeqNo;
	private int leftWindow;
	private int windowSize;
	private int timeout;

	private PipedInputStream internalInputStream;
	private PipedOutputStream appLayerStream;

	private MySocket mutex;

	private WaitForAcks waitForAcks;
	private AppLayerConnection appLayer;
	private Timer timeoutTimer;

	public MySocket(InetAddress addr, int outPort, int inPort)
			throws IOException {
		this.destAddr = addr;
		this.inPort = inPort;
		this.outPort = outPort;
		this.outSocket = new DatagramSocket();
		this.inSocket = new DatagramSocket(inPort);

		this.internalInputStream = new PipedInputStream();
		this.appLayerStream = new PipedOutputStream(internalInputStream);

		this.buffer = new Hashtable<Integer, ReliableDataPacket>();
		this.ackedNos = new HashSet<Integer>();
		this.nextSeqNo = 0;
		this.leftWindow = 0;
		this.windowSize = 5;
		this.timeout = 5000;

		this.mutex = this;

		this.waitForAcks = new WaitForAcks();
		this.appLayer = new AppLayerConnection();
		this.timeoutTimer = new Timer(true);

		(new Thread(waitForAcks, "waitAcks")).start();
		(new Thread(appLayer, "appLayerReader")).start();

	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public OutputStream getOutputStream() {
		return appLayerStream;
	}

	private synchronized void send(ReliableDataPacket packet) {
		byte[] data = packet.getByteArray();
		DatagramPacket pkt = new DatagramPacket(data, data.length, destAddr,
				outPort);
		try {
			outSocket.send(pkt);
		} catch (IOException e) {
			e.printStackTrace();
		}

		timeoutTimer.schedule(new RetransmitPacket(packet.getSeqNo()), timeout);
	}

	class RetransmitPacket extends TimerTask {
		private int seqNo;

		public RetransmitPacket(int seqNo) {
			this.seqNo = seqNo;
		}

		@Override
		public void run() {
			synchronized (mutex) {
				ReliableDataPacket pkt = buffer.get(this.seqNo);
				if (pkt != null) {
					send(pkt);
				}
			}

		}

	}

	class WaitForAcks implements Runnable {

		@Override
		public void run() {
			while (true) {
				DatagramPacket pkt = new DatagramPacket(
						new byte[ReliableAckPacket.DATA_SIZE], ReliableAckPacket.DATA_SIZE);
				try {
					inSocket.receive(pkt);
				} catch (IOException e) {
					System.err.println("broken pipeline!");
					break;
				}

				ReliableAckPacket ack = new ReliableAckPacket(pkt.getData());

				synchronized (mutex) {
					ackedNos.add(ack.getSeqNo());
					buffer.remove(ack.getSeqNo());

					int nextToSend = Math.min(nextSeqNo, leftWindow
							+ windowSize);

					// slide the window
					while (ackedNos.contains(leftWindow)) {
						ackedNos.remove(leftWindow);
						leftWindow++;
					}

					boolean inWindow = nextToSend < leftWindow + windowSize;
					while (buffer.containsKey(nextToSend) && inWindow) {
						send(buffer.get(nextToSend));
						nextToSend++;
					}
				}
			}

		}

	}

	class AppLayerConnection implements Runnable {

		@Override
		public void run() {
			while (true) {
				byte[] buf = new byte[ReliableDataPacket.DATA_SIZE];
				int bytes;
				try {
					bytes = internalInputStream.read(buf);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				if (bytes <= 0) {
					continue;
				}

				synchronized (mutex) {
					ReliableDataPacket pkt = new ReliableDataPacket(nextSeqNo,
							buf, bytes);
					nextSeqNo++;

					synchronized (buffer) {
						buffer.put(pkt.getSeqNo(), pkt);
					}

					if (pkt.getSeqNo() < leftWindow + windowSize) {
						send(pkt);
					}
				}
			}
		}

	}

}
