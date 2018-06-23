import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TFTPTools {
	
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
		
		if(packet.getData()[1] == 3){ //show data size
			System.out.println("Size of data(in byte): " + (packet.getLength()-4));
		}
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
