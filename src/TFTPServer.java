import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TFTPServer {

	private static TFTPServer instance = null; //has an instance or not
	
	private String serverFolder = System.getProperty("user.dir") + "server_files"; //the directory of folder
	
	private static boolean verbose = true; //display complexity
	
	private TFTPRequestListener serverListener; 

	private boolean initialized; //initialized or not
	
	public TFTPServer() { 
		initialized = false; 
	}
	
	public static TFTPServer instanceOf() { //initialize an instance
		if (instance == null)
			instance = new TFTPServer(); 
		return instance;
	}
	
	public void TFTPReceiveAndSend() throws SocketException {
		System.out.println("Server initialized.");
		Scanner scanner = new Scanner(System.in);
		
		serverListener = new TFTPRequestListener();
		serverListener.start();
		
		initialized = true;
		
		while (true) {
			System.out.print("#- ");
			String cmd = scanner.nextLine().toLowerCase();
			if (cmd.equals("quit") || cmd.equals("exit")) { //terminate the server
				System.out.println("Terminating client");
				serverListener.killThread();
				scanner.close();
			    return;
			} else if (cmd.equals("verbose")) { //change verbose pattern
				toggleVerbosity();
			} else if (cmd.length() == 0) { //empty
			} else { //invalid
				System.out.println("Invalid command");
				continue;
			}
		}
	}
	
	public static void printPacketInfo(boolean isSend, DatagramPacket packet) { //print packet information
		if(isSend){
			System.out.println("\nServer-Sending packet");
			System.out.println("To Host: " + packet.getAddress());
		} else {
			System.out.println("\nServer-Receiving packet");
			System.out.println("From Host: " + packet.getAddress());
		}
		
		//opcode
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
		
		if(packet.getData()[1] == 1 || packet.getData()[1] == 2){ //RRQ or WRQ
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
		
		if((packet.getData()[1] == 3) || (packet.getData()[1] == 4)){ //DATA or ACK
			System.out.print("Packet Number: ");
			System.out.println((((int) (packet.getData()[2] & 0xFF)) << 8) + (((int) packet.getData()[3]) & 0xFF));
		}
		
		if(packet.getData()[1] == 3){ //show data size
			System.out.println("Size of data(in byte): " + (packet.getLength()-4));
		}
	}
	
	public static void receiveFromClient(DatagramSocket socket, DatagramPacket packet) { //receive packet
		try {
			socket.receive(packet);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void sendToClient(DatagramSocket socket, DatagramPacket packet) { //send packet
		try {
		   socket.send(packet);
		} catch (IOException e) {
		   e.printStackTrace();
		   System.exit(1);
		}
	}
	   
	public void toggleVerbosity() { //change display complexity
		verbose = (!verbose);
		if(verbose) {
			System.out.println("VERBOSE_ON");
			serverListener.toggleVerbosity();
		} else {
			System.out.println("VERBOSE_OFF");
			serverListener.toggleVerbosity();
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, SocketException {
		TFTPServer server = TFTPServer.instanceOf();
		server.TFTPReceiveAndSend();
	}
}
