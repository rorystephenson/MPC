package thelollies.mpc.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Thread used to query the server's status (used to see if music is playing
 * and if the playlist is set to shuffle).
 * @author thelollies
 *
 */

public class StatusThread extends Thread{

	private String address;
	private int port;

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;
	
	private MPCStatus status;
	
	private boolean failed = false;

	public StatusThread(String address, int port){

		this.address = address;
		this.port = port;

	}

	@Override
	public void run(){

		// Establish socket connection
		try{
			sock = new Socket();
			sock.connect(new InetSocketAddress(address, port), MPC.TIMEOUT);
			
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Clear version number from buffer
			in.readLine();

			checkPlayingStatus();

		} catch(Exception e){
			failed = true;
		}
		try{
			sock.close();
			in.close();
			out.close();
		} catch(Exception e){}
	}

	/**
	 * Determines whether a song is playing and whether the MPD server
	 * is set to shuffle.
	 * @throws IOException
	 */
	private void checkPlayingStatus() throws IOException {
		out.println("status");

		boolean playing = false;
		boolean shuffling = false;
		
		String response;
		while((response = in.readLine()) != null){
			if(response.equals("OK")){break;}
			if(response.startsWith("state: ")){
				String state = response.substring(7);
				playing = state.equals("play") ? true : false;
			}
			if(response.startsWith("random: ")){
				int shuffleValue = Integer.parseInt(response.substring(8));
				shuffling = shuffleValue == 1 ? true : false;
			}
		}
		status = new MPCStatus(playing, shuffling);
	}

	/**
	 * @return MPCStatus representing the required status information of the MPD server
	 */
	public MPCStatus getStatus(){
		return status;
	}
	
	public boolean failed(){
		return failed;
	}
}