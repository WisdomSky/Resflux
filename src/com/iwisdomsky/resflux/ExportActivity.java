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

public class ExportActivity extends Activity implements View.OnClickListener
{

	// progress bar shown when loading packages list
 	private LinearLayout mProgress;
	
	// the listview which will be populated with
	private ListView mListView;
	
	// list of all export-able packages with mods
	private ArrayList<String> mPackagesList;
	
	// the export button
	private Button mExport;
	
	// the holder of user-selected packages to be exported
	private ArrayList<String> mExportList;
	
	// progress dialog shown when exporting is started
	private ProgressDialog mExporting;
	
	// the adapter to be used, ofcourse
	private PackageListAdapter mAdapter;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export);
		// create the packages dir if not found
		Utils.mkDirs(getFilesDir(),"packages");
		
		// initializations
		mExportList = new ArrayList<String>();
		mPackagesList = new ArrayList<String>();
		mProgress = new LinearLayout(this);
		mProgress.setOrientation(LinearLayout.HORIZONTAL);
		mProgress.setGravity(Gravity.CENTER);
		mProgress.addView(new ProgressBar(this));
		mListView = (ListView)findViewById(R.id.packagelist);		
		mListView.addFooterView(mProgress);
		mAdapter = new PackageListAdapter(ExportActivity.this,mPackagesList);
		mListView.setAdapter(mAdapter);
		mExport = ((Button)findViewById(R.id.export));
		mExport.setOnClickListener(this);
		mExport.setEnabled(false);
		((Button)findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View p1)
			{
				finish();
			}
		});
		
		// start loading packages list
		new Thread(loadPackagesList()).start();
				
	}
	
	// export button on click callback
	public void onClick(View p1)
	{
	 	if ( mExportList.size() == 0 )
			return;
		final EditText et = new EditText(this);
		et.setSingleLine(true);
		et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		new AlertDialog.Builder(this)
		.setTitle("Set label:")
		.setView(et)
		.setNeutralButton("Export", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface p1, int p2)
			{
				final String file_name = et.getText().toString().trim();
				new Thread(new Runnable(){
					public void run()
					{
						exportModPackages(file_name);
						runOnUiThread(new Runnable(){
							public void run()
							{
								mExporting.dismiss();
							}
						});
					}			
				}).start();
				mExporting = new ProgressDialog(ExportActivity.this);
				mExporting.setCancelable(false);
				mExporting.setIndeterminate(true);
				mExporting.setMessage("Exporting...");
				mExporting.setOnDismissListener(new DialogInterface.OnDismissListener(){
					public void onDismiss(DialogInterface p1)
					{
						
						Toast.makeText(ExportActivity.this,"Export complete!",Toast.LENGTH_SHORT).show();
						finish();
					}
				});
				mExporting.show();				
			}
		}).create().show();
		
	}
	
	// export method
	private void exportModPackages(String filename){
		//Toast.makeText(this,mExportList.toString(),Toast.LENGTH_SHORT).show();	
		try
		{
			// get export dir handle
			File export = new File(Environment.getExternalStorageDirectory(),"Resflux");
			// be sure that the dirs exist
			export.mkdirs();
			// get export file handle
			export = new File(export,filename.replaceAll("[^a-zA-Z0-9_\\.]","_")+".resflux.zip");
			// get export file output stream for writing
			FileOutputStream f = new FileOutputStream(export);
			// get zip output stream for writing entries
			ZipOutputStream zos = new ZipOutputStream(f);
			// add icon
			FileInputStream icon = new FileInputStream(new File(getFilesDir(),"icon.png"));
			ZipEntry icon_entry = new ZipEntry("icon.png");
			zos.putNextEntry(icon_entry);
			int li;byte[] bufferi = new byte[Constants.BUFFER_SIZE];
			while ( (li = icon.read(bufferi)) != -1 )
				zos.write(bufferi,0,li);
			zos.closeEntry();
			icon.close();
			// get script's input stream 
			InputStream in = getSerializedModPackagesStream(filename);
			// create zip entry
			ZipEntry script = new ZipEntry("Resflux.ini");
			// put the zip entry
			zos.putNextEntry(script);
			// start writing to entry
			int l;byte[] buffer = new byte[Constants.BUFFER_SIZE];
			while ( (l = in.read(buffer)) != -1 )
				zos.write(buffer,0,l);
			// close the entry after writing
			zos.closeEntry();
			in.close();
			// each packages
			for ( int i=0;i<mExportList.size();i++) {
				// get file handle
				File drawable_dir = new File(getFilesDir(),"packages/"+mExportList.get(i)+"/drawable");
				// list contents
				File[] drawables = drawable_dir.listFiles();
				// if not empty
				if ( drawables.length>0 )
					// each drawable
					for ( File drawable : drawables )
						// if a png drawable
						if ( drawable.getName().endsWith(".png") ) {
							FileInputStream din = new FileInputStream(drawable);
							ZipEntry entry = new ZipEntry("packages/"+mExportList.get(i)+"/drawable/"+drawable.getName());		
							zos.putNextEntry(entry);
							int dl;byte[] dbuffer = new byte[Constants.BUFFER_SIZE];
							while ( (dl = din.read(dbuffer)) != -1 )
								zos.write(dbuffer,0,dl);
							zos.closeEntry();
							din.close();
						}
			}
			// close the zip file
			zos.close();
		}
		catch (Exception e)
		{
			//Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
		}
	}
	
	// Resflux.ini writer method
	private InputStream getSerializedModPackagesStream(String file_name){
			final StringBuilder script = new StringBuilder();
			script.append("###########################################\n");
			script.append("##                                       ##\n");
			script.append("##                Resflux                ##\n");
			script.append("##         Import Manifest File          ##\n");
			script.append("##                                       ##\n");
			script.append("##   (Auto-generated. Do not modify)     ##\n");
			script.append("##                                       ##\n");
			script.append("##  Developer: @WisdomSky of XDA-Devs    ##\n");
			script.append("##  Contact:   http://fb.me/WisdomSky    ##\n");	
			script.append("##                                       ##\n");
			script.append("###########################################\n");
			script.append("\n\n");		
			script.append("# set custom label\n");
			script.append("resflux.label = \""+file_name+"\"\n");
			script.append("# add icon\n");
			script.append("resflux.icon = icon.png\n");
			script.append("# set description\n");
			script.append("resflux.desc = \"No description\"\n\n");
			script.append("# magic directory\n");
			script.append("ini.magic_dir = packages\n\n");
			script.append("# start\n\n");
			// each packages
			for (int i=0;i<mExportList.size();i++){
				// get package dir handle
				File pkg = new File(getFilesDir(),"packages/"+mExportList.get(i));
				// list package sub-dirs
				File[] pkg_res = pkg.listFiles();
				// check if there sub-dirs
				if ( pkg_res.length > 0 ){
					// write package name
					script.append(" ["+pkg.getName()+"]\n");
					// each of sub-dirs
					for ( File ress : pkg_res){
						// list sub-dir contents
						File[] res_c = ress.listFiles();
						// if sub-dir has contents
						if ( res_c.length > 0) {
							// each contents
							for (File res : res_c ) {
								// ignore png drawables
								if ( ress.getName().equals("drawable") && res.getName().endsWith(".png") )
									continue;
								try
								{
									// write key value pairs
									BufferedReader r = new BufferedReader(new FileReader(res));
									script.append("  "+ress.getName()+"."+res.getName()+" = "+r.readLine()+"\n");
								}
								catch (Exception e)
								{}

								
							}
							
						}
					}
					script.append("\n");
				}					
			}
			return new ByteArrayInputStream(script.toString().getBytes());
	}
	
	// load packages method
	private Runnable loadPackagesList(){
		return new Runnable(){
			@Override
			public void run(){				
				PackageManager pm = getPackageManager();
				final List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
				File packages_dir = new File(getFilesDir(),"packages");
					// each packages
					for (File p : packages_dir.listFiles())
						// if package has mods
						if (isPackageDirHasContents(p))
							try{
								apps.add(pm.getApplicationInfo(p.getName(), 0));
							} catch (PackageManager.NameNotFoundException e) {}
						// if the dir has no existing mods then do some cleaning
						else 
							Utils.deleteFile(p);
				// sort packages alphabetically
				Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pm));			
				for ( int i=0; i<apps.size(); i++ ) {		
					final int ii=i;
					runOnUiThread(new Runnable(){
						public void run(){	
							mPackagesList.add(apps.get(ii).packageName);
							mAdapter.notifyDataSetChanged();
						}
					});
				}
				// callback				
				runOnUiThread(new Runnable(){
					public void run(){		
						loadPackagesCallback();
					}
				});
			}	
		};	
	}

	// method invoked after loading packages
	private void loadPackagesCallback(){
		mExport.setEnabled(true);
		mListView.removeFooterView(mProgress);			
		mAdapter.notifyDataSetChanged();	
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
			{				
				if ( p3==mPackagesList.size() )return;	
				if ( mExportList.contains(mPackagesList.get(p3)) ){
					mExportList.remove(mPackagesList.get(p3));
					p2.setBackgroundColor(0x00000000);
				} else {
					mExportList.add(mPackagesList.get(p3));
					p2.setBackgroundResource(R.drawable.button_4);
				}
				
			}
		});
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
				public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
				{				
					final View item = p2;
					final int i = p3;
					new AlertDialog.Builder(ExportActivity.this)
					.setTitle("Reset")
					.setMessage("Do you want to clear all modifications made on this package?")
					.setNegativeButton("No",null)
					.setPositiveButton("Delete", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface p1, int p2)
						{
							Utils.deleteFile(new File(getFilesDir(),"packages/"+mPackagesList.get(i)));	
							item.setBackgroundColor(0x00000000); 
							if ( mExportList.contains(mPackagesList.get(i)) ){
								mExportList.remove(mPackagesList.get(i));
							}
								mPackagesList.remove(i);
								mAdapter.notifyDataSetChanged();
								
													
						}
					}).create().show();
					return false;
				}
		});
	}

	// check if the packages dir has packages with mods
	public boolean isPackageDirHasContents(File package_dir){
		for ( File sub : package_dir.listFiles() )
			if ( sub.listFiles().length > 0) {
				return true;
			}
		return false;
	}


}
