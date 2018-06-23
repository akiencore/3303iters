import java.io.File;
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
	   
	   private static String userFolder = System.getProperty("user.dir") + File.separator 
			   + "user_files" + File.separator; //the directory of folder
	
	   public TFTPClient() {
		   try {
			   sendReceiveSocket = new DatagramSocket();
		   } catch(SocketException se) {
			   se.printStackTrace();
			   System.exit(1);
		   }
	   }

	@SuppressWarnings("resource")
	public void TFTPSendAndReceive() throws IOException {
		   
		   System.out.println("Client: sending a packet containing:\n");

		   Scanner scanner = new Scanner(System.in);
		   
		   byte opCode = -1;
		   
		   while(true) {
			   System.out.print("#- ");
			   String[] cmd = scanner.nextLine().split("\\s+");
			   if(cmd[0].toLowerCase().equals("read") || cmd[0].toLowerCase().equals("get")){ //RRQ
				   	if (cmd.length != 2) //ensure the filename is ready
				   		System.out.println("No available file in this name");
				   	else {
				   		opCode = 1;
				   		request(opCode, cmd[1]); //passing filename
				   	}
			   } else if (cmd[0].toLowerCase().equals("write") || cmd[0].toLowerCase().equals("send")){ //WRQ
				   if (cmd.length != 2) //ensure the filename is ready
				   		System.out.println("No available file in this name");
				   	else {
				   		opCode = 2;
				   		request(opCode, cmd[1]); //passing filename
				   	}
			   } else if (cmd[0].toLowerCase().equals("quit") || cmd[0].toLowerCase().equals("exit")) { //terminate the client
				   System.out.println("Terminating client");
				   sendReceiveSocket.close();
				   scanner.close();
			       return;
			   } else if (cmd[0].toLowerCase().equals("mode")) { //current mode of TFTP
				   if(testMode) {
					   System.out.println("Current mode is test mode. ");
				   } else {
					   System.out.println("Current mode is normal mode. ");
				   }
			   } else if (cmd[0].toLowerCase().equals("switch")) { //change the mode of TFTP
				   testMode = !testMode;
				   if(testMode) {
					   System.out.println("Change the mode into test mode. ");
				   } else {
					   System.out.println("Change the mode into normal mode. ");
				   }
			   } else if (cmd[0].toLowerCase().equals("help")) { //get a help menu
				   helpMenu();
			   } else if (cmd[0].toLowerCase().equals("verbose")) { //change the display complexity
				   toggleVerbosity();
			   } else if (cmd.length == 0 || cmd[0].length() == 0) { //empty
			   } else { //invalid command
				   System.out.println("Invalid command");
				   continue;
			   }
		   }
	   }
	   
	   public static void request(byte opCode, String filename) throws IOException {
		   byte[] fn, md;
		   String mode; 
		   File file = null;
		   
		   String filePath = getFilePath(filename);
				
		   file = new File(filePath);
		   if (file.exists() && !file.canWrite()) {
			   System.out.println("File already exists or cannot be overwritten. Please try again.\n");
			   return;
		   } else if (!file.exists()) {
			   if (!file.createNewFile()) {
				   System.out.println("Failed to create " + filename + ".\n");
				   return;
			   } else {
				   //pass
			   }
		   }
		   
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
			   TFTPTools.printPacketInfo(true, sendPacket);
		   
		   TFTPTools.toSendPacket(sendReceiveSocket, sendPacket);
		   
		   if (verbose)
			   System.out.println("Client-packet sent.\n");
		   
		   
		   //here for receiving later
		   
		   
		   byte[] data = new byte[DATA_SIZE];
		   receivePacket = new DatagramPacket(data, data.length);
		   
		   TFTPTools.toReceivePacket(sendReceiveSocket, receivePacket);
		   
		   if (verbose) {
			   TFTPTools.printPacketInfo(false, receivePacket);
			   System.out.println("Client-Receiving packet.\n");
		   }
	   }
		   
	   private static String getFilePath(String filename) {
		   return userFolder + filename;
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
	   
	   public static void main(String[] args) throws IOException {
		   TFTPClient client = new TFTPClient();
		   client.TFTPSendAndReceive();
	   }
}
