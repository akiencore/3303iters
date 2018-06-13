import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TFTPServer {
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;
	
	private String serverFolder = System.getProperty("user.dir") + "server_files";
	
	private static final int SERVER_PORT = 69;
	
	private static final int DATA_SIZE = 516;
	
	public TFTPServer() {
		try {
			sendSocket = new DatagramSocket();
			
			receiveSocket = new DatagramSocket(SERVER_PORT);
			
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public void TFTPReceiveAndSend() throws UnknownHostException {
		byte data[] = new byte[DATA_SIZE];
		receivePacket = new DatagramPacket(data, data.length);
		System.out.println("Server: wait for packet.\n");
		
		try {
			System.out.println("Waiting...");
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			System.out.print("IO Exception: likely: ");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server-Packet Received: ");
		System.out.println("From Host: " + receivePacket.getAddress());
		System.out.println("From Port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		
		String received = new String(data,0,len);
		System.out.println(received + "\n");
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		

		byte opCode = -1;
		byte blockNum = -1;
		byte[] msg = new byte[DATA_SIZE];
		
		String r = "Null";
		
		if(data[1] == 1) { //RRQ
			System.out.println("RRQ from the client");
	
			blockNum = 0;
			opCode = 3;
			r = "Server is here.";
			byte[] rn = r.getBytes();
			
			//System.arraycopy(blockNum, 0, msg, 2, 1); //problem
			System.arraycopy(rn, 0, msg, 4, rn.length); //problem??
			
			msg[1] = opCode;
			msg[3] = blockNum;

			len = 4 + rn.length;
			
		} else if(data[1]==2) { //WRQ
			System.out.println("WRQ from the client");
			
			blockNum = 0;
			opCode = 4;
			
			//System.arraycopy(blockNum, 0, msg, 2, 1); //problem
			msg[0] = 0;
			msg[1] = opCode;
			msg[2]= 0;
			msg[3] = blockNum;
			
			len = 4;
		}
		
		try {
			sendPacket = new DatagramPacket(msg, msg.length, 
					InetAddress.getLocalHost(), SERVER_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
	
		System.out.println("Server-Sending Packet: ");
		System.out.println("To Host: " + sendPacket.getAddress());
		System.out.println("To Port: " + SERVER_PORT);
		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		String sending = r;
		System.out.println(sending + "\n");
		
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	    System.out.println("Server-packet sent");
	    
	    sendSocket.close();
	    receiveSocket.close();
	}
	
	
	public static void main(String[] args) throws UnknownHostException {
		TFTPServer server = new TFTPServer();
		server.TFTPReceiveAndSend();
	}
}
