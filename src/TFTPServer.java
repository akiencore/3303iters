import java.net.DatagramPacket;
import java.util.Scanner;

public class TFTPServer {

	private static TFTPServer instance = null;

	private TFTPServerDispatcher serverWaitThread;
	@SuppressWarnings("unused")
	private boolean initialized;
	private static String directory = "ServerOutput";

	private boolean verbose = true;

	private TFTPServer() { 
		initialized = false; 
		}
	
	public static TFTPServer instanceOf() {
		if (instance == null)
			instance = new TFTPServer();

		return instance;
	}

	public void receiveAndSendTFTP() {
		System.out.println("Server Started.");
		Scanner scanner = new Scanner(System.in);
		serverWaitThread = new TFTPServerDispatcher();
		serverWaitThread.start();
		initialized = true;

		while (scanner.hasNextLine()) {
			String command = scanner.nextLine().toUpperCase();
			if (command.equals("SHUTDOWN") || command.equals("Q")) {
				System.out.println("Server: Shutdown command received. Completing remaining connection threads and shutting down.");
				serverWaitThread.killThread();
				scanner.close();
				return; 
			} else if (command.equals("VERBOSE") || command.equals("V")) {
				if (isVerbose()) {
					System.out.println("Server: Verbose mode off.");
					toggleVerbosity();
				} else {
					System.out.println("Server: Verbose mode on.");
					toggleVerbosity();
				}
			} else if(command.equals("CD")){
				System.out.println("Please enter the directory you would like to change to");
				directory = scanner.nextLine();
				System.out.println("Directory changed.");
			}
		}

		scanner.close();
	}
	
	public static String getDirectory(){
		return directory;
	}
	
	public void toggleVerbosity() {
		if (verbose) {
			verbose = false;
			serverWaitThread.toggleVerbosity();
		} else {
			verbose = true;
			serverWaitThread.toggleVerbosity();
		}
	}

	public boolean isVerbose() {
		return verbose;
	}

	public static void printPacketData(boolean send, DatagramPacket packet, boolean printContents){
		if(send){
			System.out.println("\nServer: Sending packet");
			System.out.println("To Host: " + packet.getAddress());
		} else{
			System.out.println("Server: Packet received");
			System.out.println("From Host: " + packet.getAddress());
		}

		if(packet.getData()[1] == 1){
			System.out.println("Packet Type: RRQ");
		} else if(packet.getData()[1] == 2){
			System.out.println("Packet Type: WRQ");
		} else if(packet.getData()[1] == 3){
			System.out.println("Packet Type: DATA");
		} else if(packet.getData()[1] == 4){
			System.out.println("Packet Type: ACK");
		} else if(packet.getData()[1] == 5){
			System.out.println("Packet Type: ERROR");
		} else {
			System.out.println("ERROR: packet sent with unknown opcode");
		}

		System.out.println("Host port: " + packet.getPort());
		System.out.println("Length: " + packet.getLength());

		if((packet.getData()[1] == 1) || (packet.getData()[1] == 2)){			
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
			System.out.println("Number of byte of data: " + (packet.getLength()-4));
		}

		if(packet.getData()[1] == 5){
			System.out.println("Error code information: ");
			if(packet.getData()[3] == 0){
				System.out.println("Error code: 0");
				System.out.println("Not defined, see error message (if any).");
			} else if(packet.getData()[3] == 1){
				System.out.println("Error code: 1");
				System.out.println("File not found.");
			} else if(packet.getData()[3] == 2){
				System.out.println("Error code: 2");
				System.out.println("Access violation.");
			} else if(packet.getData()[3] == 3){
				System.out.println("Error code: 3");
				System.out.println("Disk full or allocation exceeded.");
			} else if(packet.getData()[3] == 4){
				System.out.println("Error code: 4");
				System.out.println("Illegal TFTP operation.");
			} else if(packet.getData()[3] == 5){
				System.out.println("Error code: 5");
				System.out.println("Unknown transfer ID.");
			} else if(packet.getData()[3] == 6){
				System.out.println("Error code: 6");
				System.out.println("File already exists.");
			} else if(packet.getData()[3] == 7){
				System.out.println("Error code: 7");
				System.out.println("No such user.");
			}
		}
	}

	public static void main(String args[]) {
		TFTPServer server = TFTPServer.instanceOf();
		server.receiveAndSendTFTP();
	}

}