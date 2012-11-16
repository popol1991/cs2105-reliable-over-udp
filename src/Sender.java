import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Sender {
	static final String REMOTE_ADDRESS = "127.0.0.1";
	static final long SEND_INTERVAL = 25;
	static final int WINDOW_SIZE = 5;
	static final int TIMEOUT = 250;
	InetAddress destAddr;
	int inPort, outPort;
	Timer timer;

	public Sender(int outPort, int inPort) {
		try {
			this.destAddr = InetAddress.getByName(REMOTE_ADDRESS);
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		this.inPort = inPort;
		this.outPort = outPort;
		this.timer = new Timer();
	}

	public void start() {
		timer.scheduleAtFixedRate(new SenderReader(destAddr, outPort, inPort),
				0, SEND_INTERVAL);
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err
					.println("please input incoming and outcoming port number");
			System.exit(-1);
		}

		int inPort, outPort;
		outPort = Integer.parseInt(args[0]);
		inPort = Integer.parseInt(args[1]);
		Sender sender = new Sender(outPort, inPort);
		sender.start();
	}

	class SenderReader extends TimerTask {
		static final String FILE_PATH = "./input.txt";
		static final int DATA_SIZE = ReliableDataPacket.DATA_SIZE;
		MySocket senderSockets;
		OutputStream outWriter;
		InputStream reader;

		public SenderReader(InetAddress addr, int inPort, int outPort) {
			try {
				reader = new FileInputStream(new File(FILE_PATH));
				senderSockets = new MySocket(addr, inPort, outPort);
				senderSockets.setWindowSize(WINDOW_SIZE);
				senderSockets.setTimeout(TIMEOUT);
				outWriter = senderSockets.getOutputStream();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			byte[] data = new byte[DATA_SIZE];
			int bytes;
			try {
				if ((bytes = reader.read(data)) != -1) {
					if (bytes < DATA_SIZE) {
						data[bytes] = -1;
					}
					outWriter.write(data);
					outWriter.flush();
				} else {
					timer.cancel();
					senderSockets.finish();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
