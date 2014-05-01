package thelollies.mpc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TabHost;

public class ReclickableTabHost extends TabHost {

	private ClickSameTabListener clickSameTabListener;
	
    public ReclickableTabHost(Context context) {
        super(context);
    }

    public ReclickableTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentTab(int index) {
        if (index == getCurrentTab()) {
        	if(clickSameTabListener != null) clickSameTabListener.clickSameTab();
        } else {
            super.setCurrentTab(index);
        }
    }
    
    public void setClickSameTabListener(ClickSameTabListener listener){
    	this.clickSameTabListener = listener;
    }
    
    public interface ClickSameTabListener{
    	public void clickSameTab();
    }
    
    
}