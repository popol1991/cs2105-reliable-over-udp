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

public class SenderApp {
	static final String REMOTE_ADDRESS = "127.0.0.1";
	static final long SEND_INTERVAL = 500;
	static final int WINDOW_SIZE = 10;
	static final int TIMEOUT = 5000;
	InetAddress destAddr;
	int inPort, outPort;
	Timer timer;

	public SenderApp(int inPort, int outPort) {
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
		timer.scheduleAtFixedRate(new SenderReader(destAddr, inPort, outPort),
				0, SEND_INTERVAL);
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.exit(-1);
		}

		int inPort, outPort;
		inPort = Integer.parseInt(args[0]);
		outPort = Integer.parseInt(args[1]);
		SenderApp sender = new SenderApp(inPort, outPort);
		sender.start();
	}

	class SenderReader extends TimerTask {
		static final String FILE_PATH = "./input.txt";
		static final int DATA_SIZE = 997;
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
			try {
				if (reader.read(data) != -1) {
					outWriter.write(data);
					outWriter.flush();
				} else {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}