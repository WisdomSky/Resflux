package com.iwisdomsky.resflux;

import android.content.*;
import android.content.res.*;
import java.io.*;

public class AssetFile extends File
{

	private Context mContext;
	
		
	public AssetFile(Context context, String asset_name){	
		super(context.getFilesDir(), asset_name);
		File f = context.getFilesDir();
		f.setReadable(true,false);		
		this.mContext = context;				
	}

	// extracts the asset file into resflux's data dir
	public void extract(){
		try
		{
			InputStream in = mContext.getAssets().open(this.getName());
			Utils.saveToFile(in,this);
		}
		catch (IOException e)
		{}

	}
	
	// is the asset file extracted already
	public boolean isExtracted(){
		if ( this.exists() && this.isFile() && this.length()>0 )
			return true;
		return false;
	}
	
}
