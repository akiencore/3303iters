import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TFTPClient{

	   private static DatagramPacket sendPacket, receivePacket;
	   private static DatagramSocket sendReceiveSocket;
	   
	   private static final int CLIENT_PORT = 23;
		
	   private static final int DATA_SIZE = 516;
	   
	   private static boolean verbose = true;
	
	   public TFTPClient() {
		   try {
			   sendReceiveSocket = new DatagramSocket();
		   } catch(SocketException se) {
			   se.printStackTrace();
			   System.exit(1);
		   }
	   }
	   
	   @SuppressWarnings("resource")
	public void TFTPSendAndReceive() {
		   
		   String s = "Anyone there?";
		   System.out.println("Client: sending a packet containing:\n" + s);

		   Scanner scanner = new Scanner(System.in);
		   
		   
		   byte opCode = -1;
		   
		   while(true) {
			   System.out.print("#- ");
			   String cmd = scanner.nextLine().toLowerCase();
			   if(cmd.equals("read") || cmd.equals("get")){
				   opCode = 1;
				   request(opCode, s);
			   } else if (cmd.equals("write") || cmd.equals("send")){
				   opCode = 2;
				   request(opCode, s);
			   } else if (cmd.equals("quit") || cmd.equals("exit")) {
				   System.out.println("Terminating client");
				   sendReceiveSocket.close();
			       return;
			   } else if (cmd.equals("help")) {
				   helpMenu();
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
	   
	   public static void request(byte opCode, String s) {
		   byte[] fn, md;
		   String filename, mode; 
		   
		   filename = s;
		   fn = filename.getBytes();
		   
		   mode = "octet";
		   md = mode.getBytes();
		   
		   byte[] msg = new byte[DATA_SIZE];
		   System.arraycopy(fn, 0, msg, 2, fn.length);
		   msg[fn.length + 2] = 0;
		   System.arraycopy(md, 0, msg, fn.length + 3, md.length);
		   int len = (fn.length + md.length) + 4;
		   msg[len - 1] = 0; // fn.length + 4 - 1 -> last digit
		   msg[0] = opCode;
		   msg[1] = opCode;
		   
		   try {
			   sendPacket = new DatagramPacket(msg, msg.length, 
					   InetAddress.getLocalHost(), CLIENT_PORT);
		   } catch (UnknownHostException e) {
			   e.printStackTrace();
			   System.exit(1);
		   }
		   
		   if (verbose)
			   printPacketInfo(true, sendPacket);
		   
		   sendToServer(sendReceiveSocket, sendPacket);
		   
		   if (verbose)
			   System.out.println("Client-packet sent.\n");
		   
		   byte[] data = new byte[DATA_SIZE];
		   receivePacket = new DatagramPacket(data, data.length);
		   
		   receiveFromServer(sendReceiveSocket, receivePacket);
		   
		   if (verbose) {
			   printPacketInfo(false, receivePacket);
			   System.out.println("Client-packet received.\n");
		   }
	   }
	   
	   public static void sendToServer(DatagramSocket socket, DatagramPacket packet) {
		   try {
			   socket.send(packet);
		   } catch (IOException e) {
			   e.printStackTrace();
			   System.exit(1);
		   }
	   }
	   
	   public static void receiveFromServer(DatagramSocket socket, DatagramPacket packet) {
		   try {
				socket.receive(packet);
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
	   }
	   
	   public static void printPacketInfo(boolean isSend, DatagramPacket packet) {
			if(isSend){
				System.out.println("\nClient-Sending packet");
				System.out.println("To Host: " + packet.getAddress());
			} else {
				System.out.println("\nClient-Receiving packet");
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
	   
	   public static void helpMenu() {
		   System.out.println("		read/get - get a file to server" + "\n"
		   					+ "	  write/send - send a file to server" + "\n"
		   					+ "	    man/help - print this menu" + "\n"
		   					+ "		 verbose - change the complexity of display" + "\n"
				   			+ "	   exit/quit - exit the client" + "\n");
	   }
	   
	   public static void main(String[] args) {
		   TFTPClient client = new TFTPClient();
		   client.TFTPSendAndReceive();
	   }
}
