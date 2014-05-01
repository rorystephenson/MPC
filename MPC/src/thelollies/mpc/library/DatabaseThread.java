package thelollies.mpc.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import thelollies.mpc.Settings;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Handles interaction with the MPD server database. Used to fetch all song
 * information as well as to instruct the database on the server to update.
 * 
 * @author thelollies
 *
 */

public class DatabaseThread extends Thread{

	private String address;
	private int port;
	private Activity activity;

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;

	private ProgressDialog pd;

	/**
	 * Creates an instance of database thread with the specified settings
	 * 
	 * @param address
	 * @param port
	 * @param activity activity the database is being accessed from
	 */
	public DatabaseThread(String address, int port, Activity activity, ProgressDialog pd){

		this.address = address;
		this.port = port;
		this.activity = activity;
		this.pd = pd;
				
		
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

			renewServer();
			renewDatabase();
		} catch(Exception e){
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					Toast.makeText(activity, "Connection failed, check settings", Toast.LENGTH_LONG).show();
				}
				
			});
		}
		try{
			sock.close();
			in.close();
			out.close();
		} catch(Exception e){}
		pd.dismiss();
		Editor edit = PreferenceManager.getDefaultSharedPreferences(activity).edit();
		edit.putString("renewDatabase", Long.toString(System.currentTimeMillis()));
		edit.commit();
	}

	/**
	 * Instructs the server to update it's database and repeats the instruction
	 * until it is successfully executed.
	 * 
	 * @throws IOException
	 */
	private void renewServer() throws IOException {
		out.println("update");
		in.readLine();in.readLine();
	}

	/**
	 * Requests a list of all songs and their info from the server. Uses this information
	 * to populate the local database.
	 * 
	 * @throws IOException
	 */
	private void renewDatabase() throws IOException {

		out.println("listallinfo");

		List<MPCSong> songs = new ArrayList<MPCSong>(); // used to store songs before saving

		ArrayList<String> response = new ArrayList<String>();
		String line;
		while((line = in.readLine()) != null){
			if(line.equals("OK")){break;}
			response.add(line);
		}

		int count = 0;
		while(count < response.size()){

			// Song attributes
			String file = null;
			int time = 0;
			String artist = null;
			String title = null;
			String album = null;
			int track = 0;

			boolean nextSong = false;
			while(count < response.size()){
				String currentLine = response.get(count);
				
				if(currentLine.startsWith("directory: ")){
					count++;continue;} // Skip directory lines
				else if(currentLine.startsWith("file: ")){
					if(nextSong){break;}
					file = currentLine.substring(6);
					nextSong = true;
				}
				else if(currentLine.startsWith("Time: ")){
					time = Integer.parseInt(currentLine.substring(6));}
				else if(currentLine.startsWith("Artist: ")){
					artist = currentLine.substring(8);}
				else if(currentLine.startsWith("Title: ")){
					title = currentLine.substring(7);}
				else if(currentLine.startsWith("Album: ")){
					album = currentLine.substring(7);}
				else if(currentLine.startsWith("Track: ")){
					if(currentLine.contains("/")){ // Handles track numbers with num/total format
						currentLine.substring(7,currentLine.indexOf("/"));
					}
					else{
						track = toInt(currentLine.substring(7));
					}
				}
				count++;

			}
			songs.add(new MPCSong(file, time, artist, title, album, track));
		}
		
		

		SongDatabase db = new SongDatabase(activity);
		for(MPCSong song : songs){
			db.insertSong(song);
		}

	}
	
	private static int toInt(String str){
		try{
			return Integer.parseInt(str);
		}catch(NumberFormatException e){
			Log.i("Metadata Error:" , "Tried to convert '" + str + "' to int.");
			return 0;
		}
	}
	
}
