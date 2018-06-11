import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class TFTPSocket {
	private DatagramSocket socket;
	
	public TFTPSocket() {
		socket = bind();
	}
	
	public TFTPSocket(int port) {
		socket = bind(port);
	}
	
	public TFTPSocket(int port, String address) {
		InetAddress inet = null;
		try {
			inet = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		socket = bind(port, inet);
	}
	
	public void sendPacket(DatagramPacket packet) {
		try {
			socket.send(packet);
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	   
	public void receivePacket(DatagramPacket packet) {
		try {
			socket.receive(packet);
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void close() {
		socket.close();
	}
	
	public DatagramSocket getDatagramSocket() { 
		return socket;
	}
	
	private DatagramSocket bind() {
		DatagramSocket socket = null;
		   
		try {
			socket = new DatagramSocket();
		} 
		catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		   
		return socket;
	}

	private DatagramSocket bind(int port) {
		DatagramSocket socket = null;
		
		try {
			socket = new DatagramSocket(port);
		} 
		catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		   
		return socket;
	}
	
	
	private DatagramSocket bind(int port, InetAddress address) {
		DatagramSocket socket = null;
		
		try {
			socket = new DatagramSocket(port, address);
		} 
		catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		return socket;
	}
	
	
	public int getPort(){
		return socket.getLocalPort();
	}
}
