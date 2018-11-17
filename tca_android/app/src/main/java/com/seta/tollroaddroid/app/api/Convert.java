package com.seta.tollroaddroid.app.api;


public class Convert {
	
    public static float mDensity;
    public static int mWidthPixels = 0;
    public static int mHeightPixels = 0;
    
    public static int dpToPx(int dp)
    {
        return Math.round((float) dp * mDensity);
    } 
    
	public Convert() 
	{

	}

}
