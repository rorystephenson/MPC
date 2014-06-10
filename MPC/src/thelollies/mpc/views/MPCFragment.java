package thelollies.mpc.views;

/*
 * Interface for fragments which are held in the ViewPager.
 */
public interface MPCFragment{
	/**
	 * Navigates to the top level in the navigation hierarchy.
	 * */
	public void navigateTop();
	
	/**
	 * Navigates up one level in the navigation hierarchy. Returns false if
	 * it is at the top level.
	 * @return true if current navigation level is not the top
	 */
	public boolean navigateUp();
	
	/**
	 * Executes necessary updates to a fragment when the database has been 
	 * updated.
	 */
	public void dbRenewed();
}
