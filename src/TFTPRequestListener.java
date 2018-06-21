import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class TFTPRequestListener extends Thread { 
	private DatagramPacket receivePacket;
	private DatagramSocket receiveSocket;
	
	private static final int SERVER_PORT = 69; //default port of the server
	
	private static final int DATA_SIZE = 516; //data size
	
	private static boolean verbose = true; //display complexity
	
	private static boolean running = false; //is the listener running
    private static int threadNumber; //total # of threads

    TFTPRequestHandler requestHandler = null; //instance of the requestHandler

	public TFTPRequestListener() throws SocketException {
		try {
			receiveSocket = new DatagramSocket(SERVER_PORT);
	        
	        threadNumber = 0;
	        running = true;
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	
	public void run() {
		while (running) {
			byte[] data; 
		    data = new byte[DATA_SIZE];
			receivePacket = new DatagramPacket(data, DATA_SIZE);
			                                                        
			if (verbose)
				System.out.println("\nServer: wait for packet.");
			
			receiveFromClient(receiveSocket, receivePacket); 
			
			
			if(verbose) {
				printPacketInfo(false, receivePacket);
				System.out.println("Server-packet received");
			}
			
			if (!running) { //break when not running
                break;
            }
			
			try {
				Thread.sleep(5000); //wait for 5 seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}

	        threadNumber++; //increase the # of threads
            requestHandler = new TFTPRequestHandler(threadNumber, receivePacket, data, verbose); //pass the info to handler
            requestHandler.start();
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
		
		if(packet.getData()[1] == 3){
			System.out.println("Size of data(in byte): " + (packet.getLength()-4));
		}
	}
	
	public void toggleVerbosity() { //change display complexity
		verbose = (!verbose);
		if(verbose) {
			System.out.println("VERBOSE_ON");
		} else {
			System.out.println("VERBOSE_OFF");
		}
	}

	public void killThread() {
        running = false;
        receiveSocket.close();
    }
}