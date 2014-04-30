package thelollies.mpc.library;

public class MPCStatus {
	
	public final boolean playing;
	public final boolean shuffling;
	public final Integer volume;
	
	public MPCStatus(boolean playing, boolean shuffling, Integer volume){
		this.playing = playing;
		this.shuffling = shuffling;
		this.volume = volume;
	}
}
