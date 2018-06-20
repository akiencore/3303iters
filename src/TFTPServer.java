import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TFTPServer {

	private static TFTPServer instance = null;
	
	private String serverFolder = System.getProperty("user.dir") + "server_files";
	
	private static boolean verbose = true;
	
	private TFTPRequestListener serverListener;

	private boolean initialized;
	
	public TFTPServer() { 
		initialized = false; 
	}
	
	public static TFTPServer instanceOf() {
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
			if (cmd.equals("quit") || cmd.equals("exit")) {
				System.out.println("Terminating client");
				serverListener.killThread();
				scanner.close();
			    return;
			} else if (cmd.equals("verbose")) {
				verbose = (!verbose);
				if(verbose) {
					System.out.println("VERBOSE_ON");
				} else {
					System.out.println("VERBOSE_OFF");
				}
			} else if (cmd.length() == 0) {
				//pass
			} else {
				System.out.println("Invalid command");
				continue;
			}
		}
	}
	
	public static void printPacketInfo(boolean isSend, DatagramPacket packet) {
		if(isSend){
			System.out.println("\nServer-Sending packet");
			System.out.println("To Host: " + packet.getAddress());
		} else {
			System.out.println("\nServer-Receiving packet");
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
			serverListener.toggleVerbosity();
		} else {
			System.out.println("VERBOSE_OFF");
			serverListener.toggleVerbosity();
		}
	}
	public static boolean isVerbose() {
		return verbose;
	}
	
	public static void main(String[] args) throws UnknownHostException, SocketException {
		TFTPServer server = TFTPServer.instanceOf();
		server.TFTPReceiveAndSend();
	}
}
