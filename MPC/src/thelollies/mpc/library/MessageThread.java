package thelollies.mpc.library;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MessageThread extends Thread{
	private String address;
	private int port;

	private Socket sock;
	private PrintWriter out;

	private boolean failed = false;

	private String message;

	public MessageThread(String address, int port, String message){

		this.address = address;
		this.port = port;
		this.message = message;

	}

	@Override
	public void run(){

		try{
			sock = new Socket();
			sock.connect(new InetSocketAddress(address, port), MPC.TIMEOUT);

			out = new PrintWriter(sock.getOutputStream(), true);

			// Send the message
			out.println(message);

		} catch(Exception e){
			failed = true;
		} 

		try{
			out.close();
			sock.close();
		} catch(Exception e){}

	}

	/**
	 * @return true if a song is playing on the MPD server
	 */
	public boolean failed(){
		return failed;
	}
}