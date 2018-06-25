import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TFTPServer {

	private static TFTPServer instance = null; //has an instance or not
	
	private static String serverFolder = System.getProperty("user.dir") + File.separator 
			+ "server_files" + File.separator; //the directory of folder
	
	private static boolean verbose = true; //display complexity
	
	private TFTPRequestListener serverListener; 

	private boolean initialized; //initialized or not
	
	public TFTPServer() { 
		initialized = false; 
	}
	
	public static TFTPServer instanceOf() { //initialize an instance
		if (instance == null)
			instance = new TFTPServer(); 
		return instance;
	}
	
	public void TFTPReceiveAndSend() throws SocketException {
		System.out.println("Server initialized.");
		Scanner scanner = new Scanner(System.in);
		
		serverListener = new TFTPRequestListener();
		serverListener.start();
		
		initialized = true;
		
		while (true) {
			System.out.print("#- ");
			String cmd = scanner.nextLine().toLowerCase();
			if (cmd.equals("quit") || cmd.equals("exit")) { //terminate the server
				System.out.println("Terminating client");
				serverListener.killThread();
				scanner.close();
			    return;
			} else if (cmd.equals("verbose")) { //change verbose pattern
				toggleVerbosity();
			} else if (cmd.length() == 0) { //empty
			} else { //invalid
				System.out.println("Invalid command");
				continue;
			}
		}
	}
	   
	public void toggleVerbosity() { //change display complexity
		verbose = (!verbose);
		if(verbose) {
			System.out.println("VERBOSE_ON");
			serverListener.toggleVerbosity();
		} else {
			System.out.println("VERBOSE_OFF");
			serverListener.toggleVerbosity();
		}
	}
	
	public static String getDirectory() {
		return serverFolder;
	}
	
	public static void main(String[] args) throws UnknownHostException, SocketException {
		TFTPServer server = TFTPServer.instanceOf();
		server.TFTPReceiveAndSend();
	}
}
