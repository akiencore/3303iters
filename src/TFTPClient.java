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
	   
	   private static final int CLIENT_PORT = 23; //default port to the error simulator
	   private static final int SERVER_PORT = 69; //default port to connect the server
		
	   private static final int DATA_SIZE = 516; //data size (in bytes)
	   
	   private static boolean verbose = true; //display complexity
	   private static boolean testMode = true; //testMode
	   private static int destPort = CLIENT_PORT; //the port of sendPacket 
	
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
			   if(cmd.equals("read") || cmd.equals("get")){ //RRQ
				   opCode = 1;
				   request(opCode, s);
			   } else if (cmd.equals("write") || cmd.equals("send")){ //WRQ
				   opCode = 2;
				   request(opCode, s);
			   } else if (cmd.equals("quit") || cmd.equals("exit")) { //terminate the client
				   System.out.println("Terminating client");
				   sendReceiveSocket.close();
				   scanner.close();
			       return;
			   } else if (cmd.equals("mode")) { //change the mode of TFTP
				   testMode = !testMode;
				   if(testMode) {
					   System.out.println("Current mode is test mode. ");
				   } else {
					   System.out.println("Current mode is normal mode. ");
				   }
			   } else if (cmd.equals("help")) { //get a help menu
				   helpMenu();
			   } else if (cmd.equals("verbose")) { //change the display complexity
				   toggleVerbosity();
			   } else if (cmd.length() == 0) { //empty
			   } else { //invalid command
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
		   msg[1] = opCode;
		   
		   if(testMode) {
			   destPort = CLIENT_PORT;
		   } else {
			   destPort = SERVER_PORT;
		   }
		   
		   try {
			   sendPacket = new DatagramPacket(msg, msg.length, 
					   InetAddress.getLocalHost(), destPort);
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
			   System.out.println("Client-Receiving packet.\n");
		   }
	   }
	   
	   public static void sendToServer(DatagramSocket socket, DatagramPacket packet) { //send packet
		   try {
			   socket.send(packet);
		   } catch (IOException e) {
			   e.printStackTrace();
			   System.exit(1);
		   }
	   }
	   
	   public static void receiveFromServer(DatagramSocket socket, DatagramPacket packet) { //receive packet
		   try {
				socket.receive(packet);
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
	   }
	   
	   public static void printPacketInfo(boolean isSend, DatagramPacket packet) { //print packet information
			if(isSend){
				System.out.println("\nClient-Sending packet");
				System.out.println("To Host: " + packet.getAddress());
			} else {
				System.out.println("\nClient-Receiving packet");
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
	   
	   public static void helpMenu() { //all commands
		   System.out.println("		read/get - get a file to server\n"
		   					+ "	  write/send - send a file to server\n"
		   					+ "	    man/help - print this menu\n"
		   					+ "		 verbose - change the complexity of display\n"
		   					+ "			mode - change the mode of TFTP\n"
				   			+ "	   exit/quit - exit the client\n");
	   }

	   public void toggleVerbosity() { //change display complexity
		   verbose = (!verbose);
		   if(verbose) {
			   System.out.println("VERBOSE_ON");
		   } else {
			   System.out.println("VERBOSE_OFF");
		   }
	   }
	   
	   public static void main(String[] args) {
		   TFTPClient client = new TFTPClient();
		   client.TFTPSendAndReceive();
	   }
}
