package thelollies.mpc.models;

import mpc.MPCQuery;

public class ListState{
	public final ListState parent;
	public final MPCQuery query;
	private int y;
	
	public ListState(ListState parent, MPCQuery query){
		this.parent = parent;
		this.query = query;
		this.y = 0;
	}
	
	public int getY(){
		return y;
	}

	public void setY(int y){
		this.y = y;
	}
}