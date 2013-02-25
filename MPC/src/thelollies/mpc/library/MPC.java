package thelollies.mpc.library;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import android.content.Context;

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
		try {thread.join();} catch (InterruptedException e) {e.printStackTrace();}
	}

	/**
	 * Sends an instruction to MPD to play the song at specified index
	 * 
	 * @param index position in the playlist of the song to play
	 */
	public void play(final int index){
		Thread thread = new Thread(){
			@Override
			public void run(){
				try{
					Socket sock = new Socket(address, port);
					BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

					// Remove the version number from the buffer
					in.readLine();

					// Play the song
					out.println("play " + index);

					sock.close();
					in.close();
					out.close();

				} catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		thread.start();
		try{thread.join();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * Sends a request to continue playback from where it was paused
	 */
	public void play(){
		Thread thread = new Thread(){
			@Override
			public void run(){
				try{
					Socket sock = new Socket(address, port);
					PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

					// Play the song
					out.println("play");

					sock.close();
					out.close();

				} catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		thread.start();
		try{thread.join();}catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * Sends a request to the MPD server to pause playback
	 */
	public void pause(){
		Thread thread = new Thread(){
			@Override
			public void run(){
				try{
					Socket sock = new Socket(address, port);
					PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

					// Play the song
					out.println("pause");

					sock.close();
					out.close();

				} catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		thread.start();
		try{thread.join();}catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * Sends a request to the MPD server to move playback to the previous song
	 * in the current playlist
	 */
	public void previous(){
		Thread thread = new Thread(){
			@Override
			public void run(){
				try{
					Socket sock = new Socket(address, port);
					PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

					// Play the song
					out.println("previous");

					sock.close();
					out.close();

				} catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		thread.start();
		try{thread.join();}catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * Sends a request to the MPD server to move playback to the next song in
	 * the current playlist
	 */
	public void next(){
		Thread thread = new Thread(){
			@Override
			public void run(){
				try{
					Socket sock = new Socket(address, port);
					PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

					// Play the song
					out.println("next");

					sock.close();
					out.close();

				} catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		thread.start();
		try{thread.join();}catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * Queries the MPD server's status to determine if a song is playing
	 * 
	 * @return true if a songs is playing on the MPD server
	 */
	public boolean isPlaying(){
		StatusThread thread = new StatusThread(address, port);
		thread.start();
		try {thread.join();} catch (InterruptedException e) {e.printStackTrace();}
		return thread.getPlaying();
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
		try {thread.join();} catch (InterruptedException e) {e.printStackTrace();}
	}

}