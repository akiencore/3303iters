import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TFTPRequestHandler extends Thread {
	private static boolean verbose = true;
	
	private static final int DATA_SIZE = 516;
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocket;
	private int threadNumber;
	private byte[] data;
	private InetAddress cAdd;
	
	public TFTPRequestHandler(int threadNum, DatagramPacket packet, byte[] received, boolean verb) {
		threadNumber = threadNum;
		receivePacket = packet;
		data = received;
		verbose = verb;
	}

	public void run() {
		cAdd = receivePacket.getAddress();
		
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if (verbose)
			System.out.println("Total processing thread number: " + threadNumber + ". ");
		
		byte opCode = -1;
		byte blockNum = -1;
		byte[] msg = new byte[DATA_SIZE];
		
		String r = "Null";
		
		if(data[1] == 1) { //RRQ
			blockNum = 0;
			opCode = 3;
			r = "Server is here.";
			byte[] rn = r.getBytes();
			
			System.arraycopy(rn, 0, msg, 4, rn.length);
				
			msg[1] = opCode;
			msg[3] = blockNum;
		} else if(data[1] == 2) { //WRQ
			blockNum = 0;
			opCode = 4;
			
			msg[0] = 0;
			msg[1] = opCode;
			msg[2]= 0;
			msg[3] = blockNum;
		}
		
			
		sendPacket = new DatagramPacket(msg, msg.length, 
				cAdd, receivePacket.getPort());
		
		
		if(verbose)
			TFTPServer.printPacketInfo(true, sendPacket);
			
		sendToClient(sendSocket, sendPacket);
		
		if(verbose)
			System.out.println("Server-packet sent");
	}
	
	public static void receiveFromClient(DatagramSocket socket, DatagramPacket packet) {
		try {
			socket.receive(packet);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void sendToClient(DatagramSocket socket, DatagramPacket packet) {
		try {
		   socket.send(packet);
		} catch (IOException e) {
		   e.printStackTrace();
		   System.exit(1);
		}
	}
	
	public void toggleVerbosity() {
		verbose = (!verbose);
		if(verbose) {
			System.out.println("VERBOSE_ON");
		} else {
			System.out.println("VERBOSE_OFF");
		}
	}
	
}
