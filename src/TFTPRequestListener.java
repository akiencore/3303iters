import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TFTPRequestListener extends Thread { 
	private DatagramPacket receivePacket;
	private DatagramSocket receiveSocket;
	
	private static final int SERVER_PORT = 69; //default port of the server
	
	private static final int DATA_SIZE = 516; //data size
	
	private static boolean verbose = true; //display complexity
	
	private static boolean running = false; //is the listener running
    private static int threadNumber; //total # of threads
    
    private static String terminal = "Server";

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
				System.out.println("\nServer: waiting for packet.");
			
			TFTPTools.toReceivePacket(receiveSocket, receivePacket); 
			
			
			if(verbose) {
				TFTPTools.printPacketInfo(terminal, false, receivePacket);
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