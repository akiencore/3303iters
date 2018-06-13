import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TFTPClient{

	   DatagramPacket sendPacket, receivePacket;
	   DatagramSocket sendReceiveSocket;
	   

	   private static final int SERVER_PORT = 69;
		
	   private static final int DATA_SIZE = 516;
	
	   public TFTPClient() {
		   try {
			   sendReceiveSocket = new DatagramSocket();
		   } catch(SocketException se) {
			   se.printStackTrace();
			   System.exit(1);
		   }
	   }
	   
	   public void TFTPSendAndReceive() {
		   byte[] fn, md;
		   String filename, mode; 
		   
		   String s = "Anyone there?";
		   System.out.println("Client: sending a packet containing:\n" + s);
		   

		   System.out.println("The request is a: ");
		   @SuppressWarnings("resource")
		   Scanner scanner = new Scanner(System.in);
		   String pattern = scanner.nextLine().toLowerCase();
		   
		   byte opCode = -1;
		   if(pattern.equals("rrq") || pattern.equals("read")){
			   opCode = 1;
		   } else if (pattern.equals("wrq") || pattern.equals("write")){
			   opCode = 2;
		   } else {
			   System.out.println("Invalid request, exit");
			   System.exit(1);
		   }
		   
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
					   InetAddress.getLocalHost(), SERVER_PORT);
		   } catch (UnknownHostException e) {
			   e.printStackTrace();
			   System.exit(1);
		   }
		   
		   System.out.println("Client-Sending Packet: ");
		   System.out.println("To Host: " + sendPacket.getAddress());
		   System.out.println("To Port: " + sendPacket.getPort());
		   System.out.println("Length: " + len);
		   System.out.print("Containing: ");
		   String sending = s;
		   System.out.println(sending);
		   
		   sendToServer(sendReceiveSocket, sendPacket);
		   
		   System.out.println("Client-Packet Sent.\n");
		   	   
		   
		   byte[] data = new byte[DATA_SIZE];
		   receivePacket = new DatagramPacket(data, data.length);
		   
		   receiveFromServer(sendReceiveSocket, sendPacket);
		   
		   System.out.println("Client-Receiving Packet: ");
		   System.out.println("From Host: " + receivePacket.getAddress());
		   System.out.println("From Port: " + receivePacket.getPort());
		   len = receivePacket.getLength();
		   System.out.println("Length: " + len);
		   System.out.print("Containing: ");
		   String receiving = new String(data,0,len);
		   System.out.println(receiving);

		   opCode = data[1];
		   if(opCode==4)
			   System.out.println("Request acknowledged");
		   
		   sendReceiveSocket.close();
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
				System.out.println("Waiting...");
				socket.receive(packet);
			} catch(IOException e) {
				System.out.print("IO Exception: likely: ");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}
	   }
	   
	   public static void main(String[] args) {
		   TFTPClient client = new TFTPClient();
		   client.TFTPSendAndReceive();
	   }
}
