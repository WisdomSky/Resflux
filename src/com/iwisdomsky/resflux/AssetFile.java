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
			FileOutputStream os = new FileOutputStream(this);
			int read;
			byte[] buffer = new byte[Constants.BUFFER_SIZE];
			while ( (read = in.read(buffer))!=-1 )
				os.write(buffer, 0, read);
			os.flush();
			os.close();
			in.close();
			setReadable(true,false);
			setExecutable(true,false);
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
