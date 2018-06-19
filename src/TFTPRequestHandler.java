import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TFTPRequestHandler extends Thread {
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocket, receiveSocket;
	
	private static final int SERVER_PORT = 69;
	
	private static final int DATA_SIZE = 516;
	
	private static boolean verbose = true; 
	
	private static boolean running = false;
    private static int threadNumber;

	public TFTPRequestHandler() throws SocketException {
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(SERVER_PORT);
	        
	        threadNumber = 0;
	        running = true;
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	
	public void run() {
		while(true) {
			byte data[] = new byte[DATA_SIZE];
			receivePacket = new DatagramPacket(data, data.length);
			System.out.println("Server: wait for packet.\n");
			
			receiveFromClient(receiveSocket, receivePacket);
			
			if(verbose) {
				printPacketInfo(false, receivePacket);
				System.out.println("Server-packet received");
			}
			
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
				blockNum = 0;
				opCode = 3;
				r = "Server is here.";
				byte[] rn = r.getBytes();
				
				System.arraycopy(rn, 0, msg, 4, rn.length);
				
				msg[1] = opCode;
				msg[3] = blockNum;
			} else if(data[1] == 2) { //WRQ
				blockNum = 0;
				opCode = 4;
				
				msg[0] = 0;
				msg[1] = opCode;
				msg[2]= 0;
				msg[3] = blockNum;
			}
			
	        threadNumber++;
			
			try {
				sendPacket = new DatagramPacket(msg, msg.length, 
						InetAddress.getLocalHost(), receivePacket.getPort());
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}
		
			if(verbose)
				printPacketInfo(true, sendPacket);
			
			sendToClient(sendSocket, sendPacket);
			
			if(verbose)
				System.out.println("Server-packet sent");
	
	        threadNumber--;
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
	
	public void toggleVerbosity() {
		verbose = (!verbose);
		if(verbose) {
			System.out.println("VERBOSE_ON");
		} else {
			System.out.println("VERBOSE_OFF");
		}
	}

	public static boolean isVerbose() {
		return verbose;
	}
	
	public void killThread() {
        running = false;
        sendSocket.close();
        receiveSocket.close();
    }
}
