package com.iwisdomsky.resflux;

import android.content.*;
import android.graphics.drawable.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;
import java.io.*;
import android.widget.TabHost.*;


public abstract class Utils
{

	public static void deleteFile(File f){
		if ( f.isDirectory() ) {
			if ( f.listFiles().length > 0 )
				for ( File sf : f.listFiles() )
					deleteFile(sf);
			f.delete();
		} else 
			f.delete();
	}
	

	public static void saveToFile(InputStream input,File file){
		try{
			FileOutputStream out = new FileOutputStream(file);
			int read;
			byte[] buffer = new byte[Constants.BUFFER_SIZE];
			while ( (read = input.read(buffer)) != -1 )
				out.write(buffer,0,read);
			out.flush();
			out.close();
			input.close();
			file.setReadable(true,false);
			file.setExecutable(true,false);
		} catch ( Exception e ) {}
	}


	public static ImageView getImgView(Context context,int id){
		ImageView iv = new ImageView(context);
		Drawable d = context.getResources().getDrawable(id);
		iv.setImageDrawable(d);
		//iv.setPadding(10,10,10,10);
		return iv;
	}


	public static void addTab(Context context,TabHost host, String name, int icon){
		TabSpec spec = host.newTabSpec(name);
        spec.setContent(R.id.resources);
		spec.setIndicator(Utils.getImgView(context,icon));
		host.addTab(spec);
	}
	
	
	public static File mkDirs(File base, String dirname){
		File f = new File(base,dirname);		
		if( !f.exists() ){
			f.mkdirs();
			f.setReadable(true,false);
			f.setExecutable(true,false);
		}
		return f;
	}	
	
	
}
