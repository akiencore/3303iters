import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TFTPClient {

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
		   String s = "Anyone there?";
		   System.out.println("Client: sending a packet containing:\n" + s);
		   
		   byte[] msg = s.getBytes();
		   
		   try {
			   sendPacket = new DatagramPacket(msg, msg.length, 
					   InetAddress.getLocalHost(), SERVER_PORT);
		   } catch (UnknownHostException e) {
			   e.printStackTrace();
			   System.exit(1);
		   }
		   
		   System.out.println("Client: Sending Packet: ");
		   System.out.println("To Host: " + sendPacket.getAddress());
		   System.out.println("To Port: " + sendPacket.getPort());
		   int len = sendPacket.getLength();
		   System.out.println("Length: " + len);
		   System.out.print("Containing: ");
		   String sending = new String(sendPacket.getData(),0,len);
		   System.out.println(sending);
		   
		   try {
			   sendReceiveSocket.send(sendPacket);
		   } catch(IOException e) {
			   e.printStackTrace();
			   System.exit(1);
		   }
		   
		   System.out.println("Client: Packet Sent.\n");
		   
		   byte[] data = new byte[DATA_SIZE];
		   receivePacket = new DatagramPacket(data, data.length);
		   
		   try {
			   sendReceiveSocket.receive(receivePacket);
		   } catch (IOException e) {
			   e.printStackTrace();
			   System.exit(1);
		   }
		   
		   System.out.println("Client: Receiving Packet: ");
		   System.out.println("To Host: " + receivePacket.getAddress());
		   System.out.println("To Port: " + receivePacket.getPort());
		   len = receivePacket.getLength();
		   System.out.println("Length: " + len);
		   System.out.print("Containing: ");
		   String receiving = new String(data,0,len);
		   System.out.println(receiving);
		   
		   sendReceiveSocket.close();
	   }
	   
	   public void main(String[] args) {
		   TFTPClient client = new TFTPClient();
		   client.TFTPSendAndReceive();
	   }
}
