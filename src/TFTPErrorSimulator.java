import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TFTPErrorSimulator {
	private static final int CLIENT_PORT = 23; //default port to the client
	private static final int SERVER_PORT = 69; //default port to connect the server
	
	private static boolean verbose = true;
	
	private int cPort; 		  //port of the client
	private int sPort = -1;   // port of the server, -1 only before initialized
	private InetAddress cAdd; //address of the client
	private InetAddress sAdd; //address of the server
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket clientSocket, serverSocket;
	
	private static TFTPErrorSimulator instance = null; //has an instance or not
	
	private static final int DATA_SIZE = 516; //max data size (in bytes)
	
	public TFTPErrorSimulator() {
		try {
			clientSocket = new DatagramSocket(CLIENT_PORT);
			serverSocket = new DatagramSocket(); 
		} catch(SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public static TFTPErrorSimulator instanceOf() { //initialize an instance
		if(instance == null) {
			instance = new TFTPErrorSimulator();
		}
		return instance;
	}
	
	public void intermediate() throws UnknownHostException { //transfer packet between server and client
		
		System.out.println("\nError Simulator is ready.");
		byte[] data;
		sAdd = InetAddress.getLocalHost(); //default server address in the local host (this computer)
		
		while(true) {
			data = new byte[DATA_SIZE];
			if (verbose)
				System.out.println("\nSimulator is waiting for packet.");
			
			receivePacket = new DatagramPacket(data, DATA_SIZE);

			TFTPTools.toReceivePacket(clientSocket, receivePacket);
			
			if (verbose)
				TFTPTools.printPacketInfo(false, receivePacket);
			
			cAdd = receivePacket.getAddress(); //get the address of the client
			cPort = receivePacket.getPort();   //get the port of the client
			
			if (receivePacket.getData()[1] == 1 || receivePacket.getData()[1] == 2) { //RRQ or WRQ
				sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), sAdd, 69);
				TFTPTools.toSendPacket(serverSocket, sendPacket);
				if (verbose)
					TFTPTools.printPacketInfo(true,sendPacket);
				
				data = new byte[DATA_SIZE];
				if (verbose)
					System.out.println("\nSimulator is waiting for packet.");
				
				receivePacket = new DatagramPacket(data, DATA_SIZE);
						
				TFTPTools.toReceivePacket(serverSocket, receivePacket);
				if (verbose)
					TFTPTools.printPacketInfo(false,receivePacket);
				sPort = receivePacket.getPort(); //to get the port of the server
			} else { //DATA or ACK or ERROR
				if(sPort > 0) {
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), sAdd, sPort);
					TFTPTools.toSendPacket(serverSocket, sendPacket);
					if (verbose)
						TFTPTools.printPacketInfo(true,sendPacket);
					
					data = new byte[DATA_SIZE];
					if (verbose)
						System.out.println("\nSimulator is waiting for packet.");
					
					receivePacket = new DatagramPacket(data, DATA_SIZE);
							
					TFTPTools.toReceivePacket(serverSocket, receivePacket);
					if (verbose)
						TFTPTools.printPacketInfo(false,receivePacket);
				}
			}
				
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), cAdd, cPort);
					
			TFTPTools.toSendPacket(clientSocket, sendPacket);
			if (verbose)
				TFTPTools.printPacketInfo(true,sendPacket);
		}
	}
	
	public static void main( String args[] ) throws UnknownHostException {
		TFTPErrorSimulator simulator = TFTPErrorSimulator.instanceOf();
		simulator.intermediate();
	}
}
