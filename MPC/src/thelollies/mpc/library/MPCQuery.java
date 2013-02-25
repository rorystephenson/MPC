package thelollies.mpc.library;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Holds a query in a compact form which can be passed to the database
 * to fetch songs or compared with other queries rather than comparing
 * a list of results of the query to see if the lists are the same.
 * 
 * This class implmenets parcelable so that it can be passed in intents
 * between activites.
 * 
 * @author thelollies
 */

public class MPCQuery implements Parcelable{

	private int type = 0;
	private String artist = "";
	private String album = "";
	
	// Note:
	// Queries with return type MPCSong <= 3
	// Queries with return type MPCAlbum <= 5
	// Query with return type String = 6
	public final static int ALL_SONGS = 1;
	public final static int SONGS_BY_ALBUM_ARTIST = 2;
	public final static int SONGS_BY_ARTIST = 3;
	public final static int ALL_ALBUMS = 4;
	public final static int ALBUMS_BY_ARTIST = 5;
	public final static int ALL_ARTISTS = 6;
	
	/**
	 * Constructor used when the query does not require artist or album 
	 * information.
	 * @param type MPCQuery constant indicating type of query
	 */
	public MPCQuery(int type){
		this.type = type;
	}
	
	/**
	 * Constructor used when the query does not require album 
	 * information.
	 * @param type MPCQuery constant indicating type of query
	 */
	public MPCQuery(int type, String artist){
		this.type = type;
		this.artist = artist;
	}
	
	/**
	 * Constructor used when the query requires artist and album 
	 * information.
	 * @param type MPCQuery constant indicating type of query
	 */
	public MPCQuery(int type, String artist, String album){
		this.type = type;
		this.artist = artist;
		this.album = album;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof MPCQuery){
			MPCQuery q = (MPCQuery) o;
			return (type == q.getType() && 
					artist.equals(q.getArtist()) 
					&& album.equals(q.getAlbum()));
		}
		return false;
	}
	
	/**
	 * @return MPCQuery constant indicating the type of query
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * @return the query's artist
	 */
	public String getArtist(){
		return artist;
	}
	
	/**
	 * @return the query's album
	 */
	public String getAlbum(){
		return album;
	}
	
	//////// Parcelable required methods ////////
	
    @Override
	public int describeContents() {
        return 0;
    }

    /**
     * Write the object's information to the passed parcel
     */
    @Override
	public void writeToParcel(Parcel out, int flags) {
    	out.writeInt(type);
    	out.writeString(artist);
    	out.writeString(album);
    }

    /**
     * Methods required by parcelable.
     */
    public static final Parcelable.Creator<MPCQuery> CREATOR = new Parcelable.Creator<MPCQuery>() {
        @Override
		public MPCQuery createFromParcel(Parcel in) {
            return new MPCQuery(in);
        }

        @Override
		public MPCQuery[] newArray(int size) {
            return new MPCQuery[size];
        }
    };

    /**
     * Private constructor which recreates the MPCQuery from a parcel
     * @param in parcel to create MPCQuery from
     */
    private MPCQuery(Parcel in) {
        type = in.readInt();
        artist = in.readString();
        album = in.readString();
    }
}
