import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TFTPTools {
	
	public static void printPacketInfo(String terminal, boolean isSend, DatagramPacket packet) { //print packet information
		if(isSend){
			System.out.println("\n" + terminal + ": sending packet");
			System.out.println("To Host: " + packet.getAddress());
			System.out.println("To Port: " + packet.getPort());
		} else {
			System.out.println("\n" + terminal + ": receiving packet");
			System.out.println("From Host: " + packet.getAddress());
			System.out.println("From Port: " + packet.getPort());
		}
		
		//opcode
		System.out.print("The type of packet is ");
		if(packet.getData()[1] == 1){
			System.out.println("RRQ");
		} else if(packet.getData()[1] == 2){
			System.out.println("WRQ");
		} else if(packet.getData()[1] == 3){
			System.out.println("DATA");
		} else if(packet.getData()[1] == 4){
			System.out.println("ACK");
		} else if(packet.getData()[1] == 5){
			System.out.println("ERROR");
		} else {
			System.out.println("undefined");
		}
		
		
		System.out.println("The length of packet is: " + packet.getLength());
		
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
			System.out.println(getPacketNum(packet));
		}
		
		if(packet.getData()[1] == 3){ //show data size
			System.out.println("Size of data(in byte): " + (packet.getLength()-4));
		}
	}
	
	public static int getPacketNum(DatagramPacket packet) {
		return (((int) (packet.getData()[2] & 0xFF)) << 8) + (((int) packet.getData()[3]) & 0xFF);
	}
	
	public static void toReceivePacket(DatagramSocket socket, DatagramPacket packet) { //receive packet
		try {
			socket.receive(packet);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void toSendPacket(DatagramSocket socket, DatagramPacket packet) { //send packet
		try {
		   socket.send(packet);
		} catch (IOException e) {
		   e.printStackTrace();
		   System.exit(1);
		}
	}
}
