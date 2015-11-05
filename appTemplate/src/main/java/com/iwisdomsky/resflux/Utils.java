package com.iwisdomsky.resflux;

import android.content.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.*;
import java.io.*;


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


	public static LinearLayout addTabView(Context context,int id, String label){
		ImageView iv = new ImageView(context);
		Drawable d = context.getResources().getDrawable(id);
		iv.setImageDrawable(d);
		
		TextView tv = new TextView(context);
		tv.setText(label);
		tv.setGravity(Gravity.CENTER);
		tv.setTextColor(0xff000000);
		
		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);
		//ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));	
		ll.addView(tv);
		ll.addView(iv);		
				
		return ll;
	}


	public static void addTab(Context context,TabHost host, String name, int icon){
		TabSpec spec = host.newTabSpec(name);
        spec.setContent(R.id.resources);
		spec.setIndicator(Utils.addTabView(context,icon,name));
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
