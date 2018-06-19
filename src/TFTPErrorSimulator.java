import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TFTPErrorSimulator {
	private static final int CLIENT_PORT = 23;
	private static final int SERVER_PORT = 69;
	
	private static boolean verbose = true;
	
	private int cPort;
	private int sPort;
	private InetAddress cAdd;
	private InetAddress sAdd;
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket clientSocket, serverSocket;
	
	private static TFTPErrorSimulator instance = null;
	
	private static final int DATA_SIZE = 516;
	
	public TFTPErrorSimulator() {
		try {
			clientSocket = new DatagramSocket(CLIENT_PORT);
			serverSocket = new DatagramSocket(); 
		} catch(SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public static TFTPErrorSimulator instanceOf() {
		if(instance == null) {
			instance = new TFTPErrorSimulator();
		}
		return instance;
	}
	
	public void intermediate() throws UnknownHostException {
		
		System.out.println("\nError Simulator is ready.");
		byte[] data;
		

		try {
			sAdd= InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println("\nError Simulator is working.");
		
		data = new byte[DATA_SIZE];
		receivePacket = new DatagramPacket(data, DATA_SIZE);
		
		if (verbose)
			System.out.println("\nSimulator is waiting for packet.");
		
		toReceivePacket(clientSocket, receivePacket);
		if (verbose)
			printPacketInfo(false,receivePacket);
		
		cPort = receivePacket.getPort();
		cAdd = receivePacket.getAddress();
		
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), sAdd, SERVER_PORT);
		
		toSendPacket(serverSocket, sendPacket);
		if (verbose)
			printPacketInfo(true,sendPacket);
		
		data = new byte[DATA_SIZE];
		receivePacket = new DatagramPacket(data, DATA_SIZE);
	
		if (verbose)
			System.out.println("\nSimulator is waiting for packet.");
			
		toReceivePacket(serverSocket, receivePacket);
		if (verbose)
			printPacketInfo(false,receivePacket);
			
		sPort = receivePacket.getPort();
		
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), cAdd, cPort);
		
		toSendPacket(clientSocket, sendPacket);
		if (verbose)
			printPacketInfo(true,sendPacket);
		
		while(true) {
			data = new byte[DATA_SIZE];
			if (verbose)
				System.out.println("\nSimulator is waiting for packet.");
			
			receivePacket = new DatagramPacket(data, DATA_SIZE);

			toReceivePacket(clientSocket, receivePacket);
			
			if (verbose)
				printPacketInfo(false, receivePacket);
			
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), sAdd, sPort);
			
			toSendPacket(serverSocket, sendPacket);
			if (verbose)
				printPacketInfo(true,sendPacket);
			
			data = new byte[DATA_SIZE];
			if (verbose)
				System.out.println("\nSimulator is waiting for packet.");
			
			receivePacket = new DatagramPacket(data, DATA_SIZE);
					
			toReceivePacket(serverSocket, receivePacket);
			if (verbose)
				printPacketInfo(false,receivePacket);
				
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), cAdd, cPort);
					
			toSendPacket(clientSocket, sendPacket);
			if (verbose)
				printPacketInfo(true,sendPacket);
		}
	}
	
	public static void toReceivePacket(DatagramSocket socket, DatagramPacket packet) {
		try {
			socket.receive(packet);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void toSendPacket(DatagramSocket socket, DatagramPacket packet) {
		try {
		   socket.send(packet);
		} catch (IOException e) {
		   e.printStackTrace();
		   System.exit(1);
		}
	}
	
	public static void printPacketInfo(boolean isSend, DatagramPacket packet) {
		if(isSend){
			System.out.println("\nError Simulator-Sending packet");
			System.out.println("To Host: " + packet.getAddress());
		} else {
			System.out.println("\nError Simulator-Receiving packet");
			System.out.println("From Host: " + packet.getAddress());
		}
		
		if(packet.getData()[1] == 1){
			System.out.println("Type: RRQ");
		} else if(packet.getData()[1] == 2){
			System.out.println("Type: WRQ");
		} else if(packet.getData()[1] == 3){
			System.out.println("Type: DATA");
		} else if(packet.getData()[1] == 4){
			System.out.println("Type: ACK");
		} else {
			System.out.println("Packet type is undefined");
		}
		

		System.out.println("Port: " + packet.getPort());
		System.out.println("Length: " + packet.getLength());
		
		if(packet.getData()[1] == 1 || packet.getData()[1] == 2){
			System.out.print("Filename: ");
			int i = 2;
			byte fName[] = new byte[packet.getLength()];
			byte mode[] = new byte[packet.getLength()];
			while(packet.getData()[i] != 0){
				fName[i-2] = packet.getData()[i];
				i++;
			}
			System.out.println(new String(fName));
			System.out.print("Mode: ");
			i++;
			int j = 0;
			while(packet.getData()[i] != 0){
				mode[j] = packet.getData()[i];
				i++;
				j++;
			}
			System.out.println(new String(mode));
		}
		
		if((packet.getData()[1] == 3) || (packet.getData()[1] == 4)){
			System.out.print("Packet Number: ");
			System.out.println((((int) (packet.getData()[2] & 0xFF)) << 8) + (((int) packet.getData()[3]) & 0xFF));
		}
		
		if(packet.getData()[1] == 3){
			System.out.println("Size of data(in byte): " + (packet.getLength()-4));
		}
	}
	
	public static void main( String args[] ) throws UnknownHostException {
		TFTPErrorSimulator simulator = TFTPErrorSimulator.instanceOf();
		simulator.intermediate();
	}
}
