package com.iwisdomsky.resflux;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import android.content.pm.*;


public class MainActivity extends Activity
{

	ScrollView mRooms;
	ProgressBar mProgress;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		mRooms = (ScrollView) findViewById(R.id.rooms);
		mProgress = (ProgressBar) findViewById(R.id.loading);
	
		getFilesDir().mkdir();
		Utils.mkDirs(Environment.getExternalStorageDirectory(),"Resflux");
		Utils.mkDirs(getFilesDir(),"packages");		
		
		if ( false/*!isXposedInstalled()*/ ) {
			new AlertDialog.Builder(this)
			.setTitle("Can't detect Xposed!")
			.setMessage("It seems that Xposed is not yet installed in your system.\nXposed Framework is required to be installed first in order for Resflux to work.\n\nDo you want to download the Xposed Installer?")
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
			
		}
		
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

	public void compile(View v) {
		try
		{
			ApplicationInfo ai = getPackageManager().getApplicationInfo("com.iwisdomsky.resflux.compiler", 0);
			Intent i = getPackageManager().getLaunchIntentForPackage("com.iwisdomsky.resflux.compiler");
			startActivity(i);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			new AlertDialog.Builder(this)
				.setTitle("Resflux Compiler is not installed!")
				.setMessage("It seems the Resflux Compiler is not installed. You need to download and install the Resflux Compiler in order to use this feature.\n\n"+
							"Resflux Compiler will allow you to export your Resflux mods into its stand-alone xposed module form.\n\nCheck READ ME! for more details")
				.create().show();			
		}

	
	}	
	
	public void about(View v) {
		LayoutInflater i = getLayoutInflater();
		View vv = i.inflate(R.layout.about,null);
		vv.setMinimumWidth(Constants.DIALOG_MIN_WIDTH);
		vv.setMinimumHeight(Constants.DIALOG_MIN_WIDTH);	
		new AlertDialog.Builder(this)
		.setView(vv)
		.create().show();
	}
	
	public void help(View v) {	
		LayoutInflater i = getLayoutInflater();
		View vv = i.inflate(R.layout.help,null);
		vv.setMinimumWidth(Constants.DIALOG_MIN_WIDTH);
		vv.setMinimumHeight(Constants.DIALOG_MIN_WIDTH);	
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
