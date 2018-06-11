import java.net.DatagramPacket;

public class TFTPServerDispatcher extends Thread {
    public static enum OPCODE {
        RRQ(1), WRQ(2), DATA(3), ACK(4), ERROR(5);
        private final int id;
        OPCODE(int id) { this.id = id; }
        public int value() { return id; }
    }
    
    public static enum Request {READ, WRITE, ERROR};
	
    public static final byte[] READ_RESP = {0, 3, 0, 1};
    public static final byte[] WRITE_RESP = {0, 4, 0, 0};

    private static final int SERVER_PORT = 6900;

    private static final int DATA_SIZE = 516;
    
    private boolean verbose = true;

    private DatagramPacket receivePacket;
    private TFTPSocket receiveSocket;
    //private DatagramSocket receiveSocket;
    
    private int threadNumber;
    //private TFTPClientConnection clientConnection;
    private boolean isRunning;
    
    
    public TFTPServerDispatcher() {
    	receiveSocket = new TFTPSocket(SERVER_PORT);
        
        threadNumber = 0;
        isRunning = true;
    }
    
    @Override
    public void run() {
     
        if (isVerbose()) { System.out.println("Server's Wait Thread: initializing."); }
        
        while (isRunning) {
            byte[] data;
            data = new byte[DATA_SIZE];
            receivePacket = new DatagramPacket(data, data.length);
            
            if (isVerbose()) { System.out.println("Server: Waiting for packet."); }

            try {
                receiveSocket.getDatagramSocket().receive(receivePacket);
            } catch (Exception se) {
                if (!isRunning) {
                } else {
                	se.printStackTrace();
                	System.exit(1);
                }
            }
            
            if (!isRunning) {
                break;
            }
            
            threadNumber++;
            //clientConnection = new TFTPClientConnection(threadNumber, receivePacket, data, verbose);
            //clientConnection.start();
        }
        
    }
    
	public boolean isVerbose() {
		return verbose;
	}
        
    public void toggleVerbosity() {
        if (verbose == true) {
            verbose = false;
            //TFTPClientConnection.toggleVerbosity();
        } else {
            verbose = true;
            //TFTPClientConnection.toggleVerbosity();
        }
    }
    
    public void killThread() {
        isRunning = false;
        receiveSocket.close();
    }
}