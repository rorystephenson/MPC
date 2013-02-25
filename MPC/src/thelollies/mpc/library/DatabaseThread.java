package thelollies.mpc.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
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
		
		while(true){
			out.println("status");
			char[] chars = new char[1000];
			in.read(chars);
			String response = new String(chars);
			
			if(!response.contains("updating_db")){break;}
		}
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
		String response;
		// Read the database response into an ArrayList until 'OK' is reached
		while((response = in.readLine()) != null){
			if(response.equals("OK")){break;} //end loop on "OK" responses

			String file = null;
			int time = -1;
			String artist = null;
			String title = null;
			String album = null;
			int track = -1;

			for(int i = 0; i < 12; i++, response = in.readLine()){
				if(response.startsWith("directory: ")){i--;continue;} // skip and ignore "directory: ..."
				if(i==0){file = response.substring(6);}
				else if(i==2){time = Integer.parseInt(response.substring(6));}
				else if(i==3){artist = response.substring(8);}
				else if(i==4){title = response.substring(7);}
				else if(i==5){album = response.substring(7);}
				else if(i==6){track = Integer.parseInt(response.substring(7, 9));}
				else if(i>6){in.readLine();in.readLine();break;} // skip remaining data

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
