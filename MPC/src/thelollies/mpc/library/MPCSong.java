package thelollies.mpc.library;

public class MPCSong {

	public final String file;
	public final int time;
	public final String artist;
	public final String title;
	public final String album;
	public final int track;
	
	/**
	 * Holds the required information for songs
	 * @param file uri of the music file
	 * @param time length of the song
	 * @param artist artist name
	 * @param title song name
	 * @param album album name
	 * @param track track number on the CD
	 */
	public MPCSong(String file, int time, String artist, String title, String album, int track){
		this.file = file;
		this.time = time;
		this.artist = artist == null ? "Unknown Artist" : artist;
		this.title = title == null ? file : title;
		this.album = album == null ? "Unknown Album" : album;
		this.track = track;
	}
	
	@Override
	public String toString(){
		return title;
	}
}
