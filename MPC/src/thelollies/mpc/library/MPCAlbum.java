package thelollies.mpc.library;

/**
 * Holds the required information of an album and allows comparison
 * between albums.
 * 
 * @author thelollies
 */

public class MPCAlbum {

	public final String artist;
	public final String title;
	
	// Special field which denotes if this is an 'All' album
	// which is used to play all songs by an artist.
	private boolean isAll = false;

	/**
	 * Instantiates an MPCAlbum
	 * 
	 * @param artist name of the artist
	 * @param title name of the album
	 */
	public MPCAlbum(String artist, String title){
		this.artist = artist;
		this.title = title;
	}
	
	/**
	 * Special constructor for 'All' albums, albums used to show all of the
	 * artist's songs when selected. Set isAll to true to make this an 'All'
	 * album. Only artist name is required here.
	 * 
	 * @param artist
	 * @param title
	 * @param isAll
	 */
	public MPCAlbum(String artist, String title, boolean isAll){
		this.artist = artist;
		this.title = title;
		this.isAll = isAll;
	}

	/**
	 * @return true if this album is a special 'All' album
	 */
	public boolean isAll(){
		return isAll;
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof MPCAlbum){
			MPCAlbum otherAlbum = (MPCAlbum) other;
			return artist.equals(otherAlbum.artist) && title.equals(otherAlbum.title);
		}
		return false;
	}
}
