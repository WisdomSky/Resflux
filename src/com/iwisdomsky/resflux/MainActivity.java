package com.iwisdomsky.resflux;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;


public class MainActivity extends Activity
{

	LinearLayout mRooms;
	ProgressBar mProgress;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		mRooms = (LinearLayout) findViewById(R.id.rooms);
		mProgress = (ProgressBar) findViewById(R.id.loading);
	
		getFilesDir().mkdir();
		Utils.mkDirs(Environment.getExternalStorageDirectory(),"Resflux");
		Utils.mkDirs(getFilesDir(),"packages");		
		/*
		if ( !isXposedInstalled() ) {
			new AlertDialog.Builder(this)
			.setTitle("Xposed Not Found!")
			.setMessage("Xposed framework is not yet installed into your system.\nResflux requires the Xposed Framework in order to work.\n\nDo you want to download the Xposed Framework Installer?")
			.setPositiveButton("Download", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface p1, int p2)
				{
					String url = "http://forum.xda-developers.com/showpost.php?p=44034334&postcount=2315";
					if ( Build.VERSION.SDK_INT >= 15 )
						url = "http://repo.xposed.info/module/de.robv.android.xposed.installer";
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
					finish();
				}
			})
			.setNegativeButton("Not yet", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface p1, int p2)
				{
					finish();
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener(){
				public void onCancel(DialogInterface p1)
				{
					finish();
				}			
			}).create().show();
			
		}*/
		
		new Thread(loadReqs()).start();		
			
	}
	
	public void laboratory(View v) {
		Intent i = new Intent(MainActivity.this,LaboratoryActivity.class);
		startActivity(i);
	}
	
	public void importt(View v) {
		Intent i = new Intent(MainActivity.this,ImportActivity.class);
		startActivity(i);
	}
	
	
	public void export(View v) {
		Intent i = new Intent(MainActivity.this,ExportActivity.class);
		startActivity(i);
	}

	public void about(View v) {
		LayoutInflater i = getLayoutInflater();
		View vv = i.inflate(R.layout.about,null);
		vv.setMinimumWidth(400);
		vv.setMinimumHeight(500);	
		new AlertDialog.Builder(this)
		.setView(vv)
		.create().show();
	}
	
	
	private Runnable loadReqs(){
		return new Runnable(){
			public void run()
			{		
				new File(Environment.getExternalStorageDirectory(),"Resflux").mkdir();
				AssetFile asset1 = new AssetFile(MainActivity.this,"aapt.bin");
				AssetFile asset2 = new AssetFile(MainActivity.this,"icon.png");
				//if ( !asset.isExtracted() )
					asset1.extract();
					asset2.extract();
				runOnUiThread(new Runnable(){
					public void run()
					{
						mProgress.setVisibility(View.GONE);
						mRooms.setVisibility(View.VISIBLE);
					}
				});
			}			
		};
	}

	public boolean isXposedInstalled(){
		try
		{
			DataInputStream in = new DataInputStream(Runtime.getRuntime().exec("app_process").getErrorStream());
			String line;
			while( (line = in.readLine()) != null )
				if ( line.toLowerCase().contains("xposed") ){
					in.close();					
					return true;
				}
		}
		catch (IOException e)
		{}
		return false;
	} 
	
	
}
