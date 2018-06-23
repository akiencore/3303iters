import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class TFTPRequestHandler extends Thread {
	private static boolean verbose = true; //display complexity
	
	private static final int DATA_SIZE = 516; //data size
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocket;
	private int threadNumber; //total # of threads (passed from TFTPRequestListener)
	private byte[] data;
	
	private static InetAddress cAdd; //address of the client
	private static int cPort;		 //port of the client
	
	public TFTPRequestHandler(int threadNum, DatagramPacket packet, byte[] received, boolean verb) {
		threadNumber = threadNum;
		receivePacket = packet;
		data = received;
		verbose = verb;
		
		cAdd = receivePacket.getAddress(); //get the address of the client
		cPort = receivePacket.getPort(); //get the port of the client
	}

	public void run() {
		
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if (verbose)
			System.out.println("Total processing thread number: " + threadNumber + ". ");
		
		//not used in iter 1, but will be in 2 and later
		int error_code = -1;
		String errorMsg = "";
		
		//check opcode
		byte opCode = -1;
		byte blockNum = -1;
		byte[] msg = new byte[DATA_SIZE];
		
		if(data[1] == 1 || data[1] == 2) {
			//pass
		} else {
			error_code = 4;
			errorMsg = "Invalid opcode";
		}
		
		//check mode
		String mode = "";
		int j = 0, k = 0;
		//get two "0"s before and after "mode" bytes
		for (j = 2; j < receivePacket.getLength(); j++) {
			if (data[j] == 0) { break; }
		}

		for(k = j+1; k < receivePacket.getLength(); k++) {
			if (data[k] == 0) { break; }
		}

		mode = new String(data, j + 1, k - j - 1);
		
		if(mode.equals("netascii") || mode.equals("octet")) {
			//pass
		} else {
			error_code = 4;
			errorMsg = "Invalid mode";
		}
		
		
		String r = "Null";
		
		if(data[1] == 1) { //RRQ
			blockNum = 0;
			opCode = 3; //DATA
			r = "Server is here.";
			byte[] rn = r.getBytes();
			
			System.arraycopy(rn, 0, msg, 4, rn.length);
				
			msg[1] = opCode;
			msg[3] = blockNum;
		} else if(data[1] == 2) { //WRQ
			blockNum = 0;
			opCode = 4; //ACK
			
			msg[0] = 0;
			msg[1] = opCode;
			msg[2]= 0;
			msg[3] = blockNum;
		}
		
			
		sendPacket = new DatagramPacket(msg, msg.length, 
				cAdd, cPort);
		
		
		if(verbose)
			TFTPTools.printPacketInfo(true, sendPacket);
			
		TFTPTools.toSendPacket(sendSocket, sendPacket);
		
		if(verbose)
			System.out.println("Server-packet sent");
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
