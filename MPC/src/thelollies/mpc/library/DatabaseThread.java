package thelollies.mpc.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

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
	private Context context;

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;

	private boolean failed = false;

	/**
	 * Creates an instance of database thread with the specified settings
	 * 
	 * @param address
	 * @param port
	 * @param context activity the database is being accessed from
	 */
	public DatabaseThread(String address, int port, Context context){

		this.address = address;
		this.port = port;
		this.context = context;

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
			failed = true;
			e.printStackTrace();
		}
		try{
			sock.close();
			in.close();
			out.close();
		} catch(Exception e){}
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
						track = Integer.parseInt(currentLine.substring(7));
					}
				}
				count++;

			}
			songs.add(new MPCSong(file, time, artist, title, album, track));
		}

		SongDatabase db = new SongDatabase(context);
		for(MPCSong song : songs){
			db.insertSong(song);
		}

	}

	public boolean failed(){
		return failed;
	}

}
