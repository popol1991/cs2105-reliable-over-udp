import java.net.DatagramSocket;


public class Sender {
	private static final int PKT_SIZE = 1000;
	private static final int SEND_INTERVAL = 500;
	private static final int WINDOW_SIZE = 10;
	private Window window;
	
	public class OutThread extends Thread {
		private DatagramSocket socket;
		private int destPort;
		
		public OutThread(DatagramSocket socket, int destPort) {
			this.socket = socket;
			this.destPort = destPort;
		}
		
		public void run() {
			
		}
	}
	
}
