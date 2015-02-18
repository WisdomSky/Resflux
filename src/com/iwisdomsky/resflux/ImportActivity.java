package com.iwisdomsky.resflux;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.adapter.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import android.text.*;

public class ImportActivity extends Activity {

 	// the progress bar shown when searching for import-able zips
	private LinearLayout mProgress;
	
	// you know already whats this for
	private ListView mListView;
	
	// this will hold the path of each import-able zip 
	private ArrayList<String> mImportCandidatesList;
	
	// the progress dialog shown when importing is started
	private ProgressDialog mImporting;
	
	// the adapter to be used
	private CandidatesListAdapter mAdapter;	
	
	// the handler
	public Handler mHandler;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.importt);
		
		Utils.mkDirs(Environment.getExternalStorageDirectory(),"Resflux");
		
		
		// initializations
		mImportCandidatesList = new ArrayList<String>();
		mAdapter =  new CandidatesListAdapter(ImportActivity.this,mImportCandidatesList);
		mImporting = new ProgressDialog(this);
		mImporting.setMessage("Importing...");
		mImporting.setCancelable(false);
		mProgress = new LinearLayout(this);
		mProgress.setOrientation(LinearLayout.HORIZONTAL);
		mProgress.setGravity(Gravity.CENTER);
		mProgress.addView(new ProgressBar(this));
		mListView = (ListView)findViewById(R.id.packagelist);		
		mListView.setFastScrollEnabled(true);
		mListView.addFooterView(mProgress);
		mListView.setAdapter(mAdapter);	
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
			{				
				// do nothing if footer view is clicked
				if ( p3==mImportCandidatesList.size() )return;	
				final File f = new File(mImportCandidatesList.get(p3));
				try
				{
					// check if the selected zip file has errors by using Engine's static method getFirstError()
					String err = Engine.getFirstError(f);
					if ( err!=null ){
						Toast.makeText(ImportActivity.this,err,Toast.LENGTH_SHORT).show();	
					// if there are no errors, then proceed importing...
					} else {
						new AlertDialog.Builder(ImportActivity.this)
						.setTitle("Import")
						.setMessage("Do you want to import this file?")
						.setNegativeButton("No",null)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface p1, int p2)
							{
								mImporting.show();
								new Thread(new Runnable(){
									public void run()
									{
										// start importing
										Engine e = new Engine(ImportActivity.this);
										e.setZipFile(f);
										e.start();
										
										// dismiss dialog after when importing ia completed
										mHandler.post(new Runnable(){
											public void run()
											{
												mImporting.dismiss();
												Toast.makeText(ImportActivity.this,"Import complete!\nREBOOT your phone to apply changes.",Toast.LENGTH_SHORT).show();
											}
										});
									}
								}).start();
							}
						}).create().show();
					}
				}
				catch (Exception e)
				{}

			}
		});
		mHandler = new Handler();
		((Button)findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View p1)
			{
				finish();
			}
		});
		new Thread(loadImportCandidatesList()).start();				
	}
	
	
	
	

	
	private Runnable loadImportCandidatesList(){
		return new Runnable(){
			@Override
			public void run(){				
				scanForImportCandidates(new File(Environment.getExternalStorageDirectory(),"Resflux"));
				//scanForImportCandidates(Environment.getExternalStorageDirectory());				
				runOnUiThread(new Runnable(){
					public void run(){		
						loadImportCandidatesCallback();
					}
				});
			}	
		};	
	}


	private void loadImportCandidatesCallback(){	
		mListView.removeFooterView(mProgress);
	}

	// the method which will scan for zips
	public void scanForImportCandidates(final File file){
		if ( file.isDirectory() )
			for ( final File sub : file.listFiles(getZipFilter()) )
				scanForImportCandidates(sub);					
		else if ( isCandidateForImport(file) ) { 		
			mHandler.post(new Runnable(){
				public void run()
				{
					mImportCandidatesList.add(file.getAbsolutePath());
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}
	
	// The filter used when scanning for zips. By using a filter, searching for zips will be much faster
	public FilenameFilter getZipFilter(){
		return new FilenameFilter() {
    		public boolean accept(File dir, String name) {
        		return !name.toLowerCase().startsWith(".") && (name.toLowerCase().endsWith(".zip") || dir.isDirectory());
    		}
		};
	}
	
	// this method is used for checking if the zip file is an import-able file by check the Resflux.ini inside
	public boolean isCandidateForImport(File file){
		try
		{
			ZipFile zip = new ZipFile(file);
			Enumeration e = zip.entries();
			while ( e.hasMoreElements() )
				if ( ((ZipEntry)e.nextElement()).getName().equals("Resflux.ini") )
					return true;				
		}
		catch (IOException e)
		{}
		return false;
	}

}
