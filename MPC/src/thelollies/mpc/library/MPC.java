package thelollies.mpc.library;

import java.util.List;
import android.content.Context;
import android.widget.Toast;

/**
 * This class manages connection with the MPD server and all connections
 * should go through it.
 * 
 * @author thelollies
 */

public class MPC {

	private String address;
	private int port;
	private final Context context;
	public static final int TIMEOUT = 1000; // connection timeout (ms)

	/**
	 * This constructor accepts the instance MPC is instantiated from and sets
	 * up required connection with the MPD server.
	 * 
	 * @param context the instance of the activity this instance is called from
	 */
	public MPC(Context context){
		this.context = context;
		SettingsDatabase settings = new SettingsDatabase(context);
		this.address = settings.getAddress();
		this.port = settings.getPort();
	}

	/**
	 * Clears the database of songs on the device before asking MPD to renew its
	 * database updates with the new one.
	 */
	public void renewDatabase(){
		SongDatabase db = new SongDatabase(context);
		db.clearSongs();
		DatabaseThread thread = new DatabaseThread(address, port, context);
		thread.start();

		// Check if the connection succeeded
		try{
			thread.join();
			if(thread.failed()){
				Toast.makeText(context, "Connection failed, check settings", Toast.LENGTH_LONG).show();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Sends an instruction to MPD to play the song at specified index
	 * 
	 * @param index position in the playlist of the song to play
	 */
	public void play(int index){
		sendMessage("play " + index);
	}

	/**
	 * Sends a request to continue playback from where it was paused
	 */
	public void play(){
		sendMessage("play");
	}

	/**
	 * Sends a request to the MPD server to pause playback
	 */
	public void pause(){
		sendMessage("pause");
	}

	/**
	 * Sends a request to the MPD server to move playback to the previous song
	 * in the current playlist
	 */
	public void previous(){
		sendMessage("previous");
	}

	/**
	 * Sends a request to the MPD server to move playback to the next song in
	 * the current playlist
	 */
	public void next(){
		sendMessage("next");
	}

	/**
	 * Queries the MPD server's status to determine if a song is playing
	 * 
	 * @return true if a songs is playing on the MPD server
	 */
	public MPCStatus getStatus(){
		StatusThread thread = new StatusThread(address, port);
		thread.start();
		try {thread.join();} catch (InterruptedException e) {e.printStackTrace();}
		// Check if the connection succeeded
		try{
			thread.join();
			if(thread.failed()){
				Toast.makeText(context, "Connection failed, check settings", Toast.LENGTH_LONG).show();
			}
			else{
				return thread.getStatus();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Enques the specified list of songs on the MPD server in the order
	 * they are passed, ready for playback.
	 * 
	 * @param songs a list of MPCSongs to enque on MPD server
	 */
	public void enqueSongs(List<MPCSong> songs) {
		EnqueThread thread = new EnqueThread(address, port, songs);
		thread.start();
		// Check if the connection succeeded
		try{
			thread.join();
			if(thread.failed()){
				Toast.makeText(context, "Connection failed, check settings", Toast.LENGTH_LONG).show();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Send the specified message to the MPD server, use newline char to seperate lines
	 * @param message String to send to the MPD server
	 */
	private void sendMessage(String message){
		MessageThread thread = new MessageThread(address, port, message);
		thread.start();

		// Check if the connection succeeded
		try{
			thread.join();
			if(thread.failed()){
				Toast.makeText(context, "Connection failed, check settings", Toast.LENGTH_LONG).show();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Enables/disables the shuffle feature on the MPDServer
	 * 
	 * @param shuffle true to turn on, false to turn off
	 */
	public void shuffle(boolean shuffle) {
		if(shuffle){
			sendMessage("random 1");
		}
		else{
			sendMessage("random 0");
		}
	}

}