package thelollies.mpc.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import android.util.Log;

/**
 * Thread used to query the server's status (used to see if music is playing).
 * @author thelollies
 *
 */

public class StatusThread extends Thread{

	private String address;
	private int port;

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;
	
	private boolean playing = false;

	public StatusThread(String address, int port){

		this.address = address;
		this.port = port;

	}

	@Override
	public void run(){

		// Establish socket connection
		try{
			this.sock = new Socket(address, port);
			this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			this.out = new PrintWriter(sock.getOutputStream(), true);

			// Clear version number from buffer
			in.readLine();

			checkPlayingStatus();

			sock.close();
			in.close();
			out.close();
		} catch(Exception e){
			Log.i("Network error", "couldn't determine whether playing/paused");
			e.printStackTrace();
		}
	}

	/**
	 * Determines whether a song is playing or not.
	 * @throws IOException
	 */
	private void checkPlayingStatus() throws IOException {
		out.println("status");

		String response;
		while((response = in.readLine()) != null){
			if(response.equals("OK")){break;}
			if(response.startsWith("state: ")){
				String state = response.substring(7);
				playing = state.equals("play") ? true : false;
				System.out.println(playing);
				return;
			}
		}
	}

	/**
	 * @return true if a song is playing on the MPD server
	 */
	public boolean getPlaying(){
		return playing;
	}
}