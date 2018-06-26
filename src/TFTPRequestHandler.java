import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class TFTPRequestHandler extends Thread {
	private static boolean verbose = true; //display complexity
	
	private static final int DATA_SIZE = 516; //data size
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocket;
	private int packetNum;
	private int threadNum; //total # of threads (passed from TFTPRequestListener)
	private byte[] data;
	
	private static InetAddress cAdd; //address of the client
	private static int cPort;		 //port of the client
    
    private static String terminal = "Server";

	ArrayList<DatagramPacket> list = new ArrayList<DatagramPacket>();
	
	public TFTPRequestHandler(int threadNum, DatagramPacket packet, byte[] received, boolean verb) {
		threadNum = threadNum;
		receivePacket = packet;
		data = received;
		verbose = verb;
		
		cAdd = receivePacket.getAddress(); //get the address of the client
		cPort = receivePacket.getPort(); //get the port of the client
	}

	public void run() {
		

		list.add(receivePacket);
		
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if (verbose)
			System.out.println("Total processing thread number: " + threadNum + ". ");
		
		//not used in iter 1, but will be in 2 and later
		int error_code = -1;
		String errorMsg = "";
		
		//check opcode
		byte opCode = -1;
		byte blockNum = -1;
		byte[] msg = new byte[DATA_SIZE];
		
		if(data[1] == 1 || data[1] == 2) { //check opcode
			//pass
		} else {
			error_code = 4;
			errorMsg = "Invalid opcode";
		}

		//check mode
		String mode = "";
		int j = 0, k = 0; // indicators
		//get two "0"s before and after "mode" bytes
		for(j=2; j<receivePacket.getLength(); j++) {
			if(data[j] == 0) break;
		}
		for(k=j+1; k<receivePacket.getLength(); k++) {
			if(data[k] == 0) break;
		}

		mode = new String(data, j + 1, k - j - 1);
		
		if(mode.equals("netascii") || mode.equals("octet")) { //check mode
			//pass
		} else {
			error_code = 4;
			errorMsg = "Invalid mode";
		}
		
		
		int length = receivePacket.getLength();
		String filename = "";
		
		int l=0, m=0;
		
		for(l=2;l<length;l++) {
			if(data[l] == 0) break;
		}
		
		filename = new String(data, 2, l-2);
		
		if(data[1] == 1) { //RRQ
			if(verbose)
				System.out.println("Get a read request. ");
		} else if(data[1] == 2) { //WRQ
			if(verbose)
				System.out.println("Get a write request");
		}
		
		File file;
		
		file = new File(TFTPServer.getDirectory(), filename);
		
		if(data[1] == 1) {
			if(file.exists() && !file.isDirectory()) 
				sendFile(receivePacket.getPort(), receivePacket.getAddress(), filename);
		}
		else if(data[1] == 2) {
			if(file.exists() && !file.isDirectory())
				try {
					receiveFile(receivePacket.getPort(), receivePacket.getAddress(), filename);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
		}
	}
	
	private void receiveFile(int port, InetAddress address, String filename) throws IOException {
		byte[] ack = new byte[] {0, 4, 0, 0}; //ACK
		sendPacket = new DatagramPacket(ack, 4, address, port);
		
		if (verbose) 
			TFTPTools.printPacketInfo(terminal, true, sendPacket);
		TFTPTools.toSendPacket(sendSocket, sendPacket);
		
		File file = new File(TFTPServer.getDirectory(), filename); //new file instance
		int blockNum = 1;
		try {
			BufferedOutputStream fs = new BufferedOutputStream(new FileOutputStream(file)); //the stream to handle file transfer
			while(true) {
				byte[] data = new byte[DATA_SIZE];
				receivePacket = new DatagramPacket(data, DATA_SIZE);
				while(true) {
					TFTPTools.toReceivePacket(sendSocket, sendPacket);
					break;
				}
				if(verbose) TFTPTools.printPacketInfo(terminal, false, receivePacket); //DATA
				
				int error_code = -1;
				
				list.add(receivePacket);
				
				try {
					fs.write(receivePacket.getData(), 4, receivePacket.getLength()-4); // from position 4, length is total-4 (WRQ or RRQ)
				} catch (IOException ioe) {
					ioe.printStackTrace();
					System.exit(1);
				}
				
				packetNum = TFTPTools.getPacketNum(receivePacket);
				
				byte info[] = {0, 4, receivePacket.getData()[2], receivePacket.getData()[3]}; //last two are block numbers
				
				blockNum++;
				
				if(blockNum == 65536){
					blockNum = 0;
				}
				
				if(verbose)
					TFTPTools.printPacketInfo(terminal, true, sendPacket); //ACK
				TFTPTools.toSendPacket(sendSocket, sendPacket);
				

				if(receivePacket.getLength() < DATA_SIZE) //last one smaller than 512 bytes
					break; 
			}
			fs.close();
		} catch (FileNotFoundException nfe) {
			System.out.println("File not found");
			nfe.printStackTrace();
			System.exit(1);
		}
		System.out.println("File transfer from client to server is over. ");
	}
	
	
	
	private void sendFile(int port, InetAddress address, String filename) {
		byte[] data = new byte[] {0, 3, 0, 1}; //ACK
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
