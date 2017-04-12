package com.iwisdomsky.resflux;

import android.app.*;

public class XThread extends Thread
{

	public XThread(Runnable run){
		super(run);
	} 

	public void stopX(ExperimentActivity act){
		act.stopThread(this);
	}
	
}
