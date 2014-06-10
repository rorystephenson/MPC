package thelollies.mpc.models;

import java.io.Serializable;

import mpc.MPCQuery;

/**
 * ListState holds a parent ListState, a music query and the position
 * of the last activated item in the list that results from the query.
 * @author Rory Stephenson
 */
public class ListState implements Serializable{
	private static final long serialVersionUID = 3L;
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