import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TFTPServer {
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;
	
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
	
	public void TFTPReceiveAndSend() {
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
		
		System.out.println("Server: Packet Received: ");
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
		
		sendPacket = new DatagramPacket(data, receivePacket.getLength(), 
				receivePacket.getAddress(), receivePacket.getPort());
	
		System.out.println("Server: Sending Packet: ");
		System.out.println("To Host: " + sendPacket.getAddress());
		System.out.println("To Port: " + sendPacket.getPort());
		len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		String sending = new String(sendPacket.getData(),0,len);
		System.out.println(sending + "\n");
		
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	    System.out.println("Server: packet sent");
	    
	    sendSocket.close();
	    receiveSocket.close();
	}
	
	
	public static void main(String[] args) {
		TFTPServer server = new TFTPServer();
		server.TFTPReceiveAndSend();
	}
}
