package com.iwisdomsky.resflux;

import android.content.*;
import java.io.*;

public abstract class AaptManager 
{	
	
	
	private static DataInputStream getDump(Context context, String apk_path,String what){
		java.lang.Process proc = null;
		try
		{
			proc = Runtime.getRuntime().exec(context.getFilesDir().getAbsolutePath()+File.separator+"aapt.bin d --values "+what+" "+apk_path);
		}
		catch (IOException e)
		{}
		return new DataInputStream(proc.getInputStream());		
	}
	

	public static DataInputStream getDumpResourcesStream(Context context, String apk_path){
		return getDump(context, apk_path, "resources");
	}

}
