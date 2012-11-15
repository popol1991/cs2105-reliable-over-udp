import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReceiverApp extends Thread {
	private static final String FILE_PATH = "./output.txt";
	private int inPort, outPort;
	private FileOutputStream writer;

	public ReceiverApp(int inPort, int outPort) throws IOException {
		this.inPort = inPort;
		this.outPort = outPort;
		File file = new File(FILE_PATH);
		if (!file.exists()) {
			file.createNewFile();
		}
		this.writer = new FileOutputStream(file);
	}

	@Override
	public void run() {
		try {
			MyServerSocket socket = new MyServerSocket(inPort, outPort);
			InputStream reader = socket.getInputStream();
			byte[] buf = new byte[1024];
			while (reader.read(buf) != 0) {
				writer.write(buf);
			}
			reader.close();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.exit(-1);
		} else {
			int inPort = Integer.parseInt(args[0]);
			int outPort = Integer.parseInt(args[1]);
			try {
				(new ReceiverApp(inPort, outPort)).start();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
		}
	}
}
