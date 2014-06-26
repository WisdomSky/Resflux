package com.iwisdomsky.resflux;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.*;
import com.iwisdomsky.resflux.adapter.*;
import com.iwisdomsky.resflux.dialog.*;
import java.io.*;
import java.util.*;

public class ExperimentActivity extends TabActivity implements TabHost.OnTabChangeListener, AdapterView.OnItemClickListener
{

    /** Called when the activity is first created. */
    
	// progress dialog for mapping resources
	private ProgressDialog mProgress;
	
	// the selected package
	private AndroidPackage mPackage;
	// the res contents of the selected package
	private AndroidPackage.Resources mRes;
	
	// the thread used for mapping resources
	private XThread mThread;
	
	// obviously we know what these are for
	private ListView mListView;
	private TextView mHeaderView;
	private ResourceListAdapter mAdapter; 
	private TabHost mHost;
	
	// the cobtainers of the keys and corresponding value determined by their index
	private ArrayList<String> mRkeys;
	private ArrayList<String> mRvals; 	
	
	// these are the specified directories where mods are possibly stored
	private File mPackageDir;
	private File mPackageDrawableDir;
	private File mPackageColorDir;	
	private File mPackageStringDir;
	private File mPackageIntegerDir;
	private File mPackageBooleanDir;
	
	// the package name of the selected package
	private String mPackageName;
	
	// the current tab?
	private String mCurrentTab = "drawable"; 
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experiment);
		mPackageName = getIntent().getStringExtra("package_name");
		mHeaderView = new TextView(this);
		mHeaderView.setGravity(Gravity.CENTER);	
		mProgress = ProgressDialog.show(this, null, "Mapping resources...", true, true);
		mPackage = new AndroidPackage(this, mPackageName);
		mListView = (ListView) findViewById(R.id.resources);
		mListView.setOnItemClickListener(this);
		mProgress.setOnCancelListener(new DialogInterface.OnCancelListener(){
			public void onCancel(DialogInterface p1)
			{
				mThread.stopX(ExperimentActivity.this);
				finish();
			}
		});
		
		mkDirs();
		
		// inform the user that first time will take some time
		Toast t = Toast.makeText(this," This process will only take much time for the first time.",Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM|Gravity.CENTER,0,0);
		t.show();
		
		// start mapping
		mThread = new XThread(mapResources());
		mThread.start();
		
	 	mHost = (TabHost)findViewById(android.R.id.tabhost);
        
		// adding the tabs
		Utils.addTab(this, mHost, "drawable", R.drawable.drawable);
		Utils.addTab(this, mHost, "string", R.drawable.string);
		Utils.addTab(this, mHost, "color", R.drawable.color);
		Utils.addTab(this, mHost, "boolean", R.drawable.bool);	
		Utils.addTab(this, mHost, "integer", R.drawable.integer);	
		mHost.setOnTabChangedListener(this);
    }


	@Override
	public void onDestroy() {
		// let's cache the resources
		if ( mRes!=null && !isCached(mPackageName) ) {
			File cache_dest = new File(getCacheDir(),mPackageName+".bin");
			try
			{
				FileOutputStream fos = new FileOutputStream(cache_dest);
				ObjectOutputStream os = new ObjectOutputStream(fos);
				os.writeObject(mRes);
				os.flush();
				os.close();
			}
			catch (Exception e) {}
		}
		super.onDestroy();
	}

	// check if the package's resources are cached
	private boolean isCached(String package_name){
		for (File c : getCacheDir().listFiles() )
			if ( c.getName().startsWith(package_name) )
				return true;
		return false;
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		// block click event for the footer view
		if ( p3==mRvals.size() )return;
		final int i = p3;
		// remove the "!" 
		String key = mRkeys.get(i);
		if ( key.startsWith("!") ) {
			key = key.replaceFirst("!","");
		}
		final String fkey = key;
		
		// for color type of resources
		if ( mCurrentTab.matches("^drawable|color$") && mRvals.get(p3).matches("^#[a-fA-F\\d]+$") ){
			SetColorDialog d = new SetColorDialog(this);
			d.setColor(Color.parseColor(mRvals.get(p3)));
			d.setPositiveButton("Done", new SetColorDialog.OnClickListener(){
				public void onClick(int color)
				{		
					String s_color = ((color==0)?"00000000":Integer.toHexString(color));
					if ( !(("#"+s_color).equals(mRvals.get(i))) ){
						File copy_to = new File(mCurrentTab.equals("drawable")?mPackageDrawableDir:mPackageColorDir,new File(fkey).getName());
						InputStream in = new ByteArrayInputStream(s_color.getBytes());
						Utils.saveToFile(in,copy_to);	
						mRvals.set(i, "#" + s_color);
						String ukey = mRkeys.get(i).startsWith("!")?"":"!";
						mRkeys.set(i,ukey + mRkeys.get(i));
						mAdapter.notifyDataSetChanged();
					}				
				}
			});
			d.setNegativeButton("Reset", new SetColorDialog.OnClickListener(){
				public void onClick(int color)
					{
						new File(mCurrentTab.equals("drawable")?mPackageDrawableDir:mPackageColorDir,new File(fkey).getName()).delete();
						String tab = mCurrentTab;
						mHost.setCurrentTabByTag("boolean");
						mHost.setCurrentTabByTag(tab);
					}
				});
			d.show();
		} else		
		// for PNG drawables 
		if ( mRvals.get(p3).matches("^.*\\.(PNG|png)$") ){
			SetDrawableDialog d = new SetDrawableDialog(this);
			d.setOnItemClickListener(new SetDrawableDialog.OnItemClickListener(){
				public void onItemClick(File file)
				{
					File copy_to = new File(mPackageDrawableDir,new File(mRvals.get(i)).getName());
					try
					{
						FileInputStream in = new FileInputStream(file);
						Utils.saveToFile(in, copy_to);
					}
					catch (FileNotFoundException e)
					{}
				
					mRvals.set(i, copy_to.getAbsolutePath());
					String ukey = mRkeys.get(i).startsWith("!")?"":"!";
					mRkeys.set(i,ukey + mRkeys.get(i));
					mAdapter.notifyDataSetChanged();		
				}
			});
			d.setNegativeButton("Reset", new SetDrawableDialog.OnClickListener(){
					public void onClick()
					{
						new File(mPackageDrawableDir,new File(mRvals.get(i)).getName()).delete();
						String tab = mCurrentTab;
						mHost.setCurrentTabByTag("boolean");
						mHost.setCurrentTabByTag(tab);
					}
				});
			d.show();
			
		// for boolean	
		} else if ( mCurrentTab.equals("boolean") ) {
			SetBooleanDialog d = new SetBooleanDialog(this);
			d.setValue(Boolean.valueOf(mRvals.get(i)));
			d.setPositiveButton("Done", new SetBooleanDialog.OnClickListener(){
					public void onClick(boolean value)
					{
						if ( value != Boolean.parseBoolean(mRvals.get(i)) ) {
							File copy_to = new File(mPackageBooleanDir,new File(fkey).getName());
							InputStream in = new ByteArrayInputStream((value?"true":"false").getBytes());
							Utils.saveToFile(in,copy_to);
							mRvals.set(i,value?"true":"false");
							String ukey = mRkeys.get(i).startsWith("!")?"":"!";
							mRkeys.set(i,ukey + mRkeys.get(i));
							mAdapter.notifyDataSetChanged();
							
						}					
					}
				});
			d.setNegativeButton("Reset", new SetBooleanDialog.OnClickListener(){
					public void onClick(boolean value)
					{
						new File(mPackageBooleanDir,new File(fkey).getName()).delete();
						String tab = mCurrentTab;
						mHost.setCurrentTabByTag("integer");
						mHost.setCurrentTabByTag(tab);
					}
				});
			d.show();
		// if string or integer
		} else {
			SetTextDialog d = new SetTextDialog(this);
			if ( mCurrentTab.equals("integer") )
				d.setIntegerInputType(true);
			d.setText(mRvals.get(i));
			d.setPositiveButton("Done", new SetTextDialog.OnClickListener(){
					public void onClick(String text)
					{
						if ( !(text.equals(mRvals.get(i))) ) {
							File copy_to = new File(mCurrentTab.equals("string")?mPackageStringDir:mPackageIntegerDir,new File(fkey).getName());
							InputStream in = new ByteArrayInputStream(text.getBytes());
							Utils.saveToFile(in,copy_to);
							mRvals.set(i,text);
							String ukey = mRkeys.get(i).startsWith("!")?"":"!";	
							mRkeys.set(i,ukey + mRkeys.get(i));
							mAdapter.notifyDataSetChanged();
							
						}					
					}
				});
			d.setNegativeButton("Reset", new SetTextDialog.OnClickListener(){
					public void onClick(String text)
					{
						new File(mCurrentTab.equals("string")?mPackageStringDir:mPackageIntegerDir,new File(fkey).getName()).delete();
						String tab = mCurrentTab;
						mHost.setCurrentTabByTag("boolean");
						mHost.setCurrentTabByTag(tab);
					}
				});
			d.show();	
		}
		
		
	}


	@Override
	public void onTabChanged(String p1)
	{
		mCurrentTab = p1;
		//((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(50);
		Set<Map.Entry<String,String>> res = null;
		switch (Constants.ResourceTypes.valueOf(p1.toUpperCase())){
			case DRAWABLE: res 	= mRes.DRAWABLES.entrySet(); break;
			case STRING:   res 	= mRes.STRINGS.entrySet(); break;
			case COLOR:    res 	= mRes.COLORS.entrySet(); break;
			case BOOLEAN:  res 	= mRes.BOOLEANS.entrySet();break;
			case INTEGER:  res 	= mRes.INTEGERS.entrySet();break;
		}
			
		// resource names holder
		mRkeys = new ArrayList<String>();
		// resource values holder
		mRvals = new ArrayList<String>();
				
		// transfer keys and values into separate arraylists
		for (Map.Entry<String, String> e : res) {		
			File rf = null;
			File rfc = null;
			// check for existing replacements
			switch (Constants.ResourceTypes.valueOf(p1.toUpperCase())){
				// for drawable
				case DRAWABLE: 
					rf = new File(mPackageDrawableDir,new File(e.getValue()).getName());				
					rfc = new File(mPackageDrawableDir,e.getKey());
					if ( rf.exists() )
						mRvals.add(rf.getAbsolutePath());							
					else if ( rfc.exists() )
						try
						{
							mRvals.add("#"+new BufferedReader(new FileReader(rfc)).readLine());				
						}
						catch (IOException d)
						{}							
					else {
						// if nothing exists
						mRkeys.add(e.getKey());
						mRvals.add(e.getValue());
						break;
					}
					// if exists prepend a "!"
					mRkeys.add("!"+e.getKey());
					break;
				// for string	
				case STRING:
					rf = new File(mPackageStringDir,new File(e.getKey()).getName());
					try
					{
						if (rf.exists())
							mRvals.add(new BufferedReader(new FileReader(rf)).readLine());
						else {
							mRvals.add(e.getValue());
							mRkeys.add(e.getKey());
							break;
						}
						mRkeys.add("!"+e.getKey());
					}
					catch (IOException c)
					{}
					break;
				
				// for color
				case COLOR:    
					rf = new File(mPackageColorDir,new File(e.getKey()).getName());
					try
					{
						if (rf.exists())
							mRvals.add("#"+new BufferedReader(new FileReader(rf)).readLine());
						else {
							mRvals.add(e.getValue());
							mRkeys.add(e.getKey());
							break;
						}
						mRkeys.add("!"+e.getKey());
					}
					catch (IOException c)
					{}
					break;
					
				case BOOLEAN:
					rf = new File(mPackageBooleanDir,new File(e.getKey()).getName());
					try
					{
						if (rf.exists())
							mRvals.add(new BufferedReader(new FileReader(rf)).readLine());
						else {
							mRvals.add(e.getValue());
							mRkeys.add(e.getKey());
							break;
						}
						mRkeys.add("!"+e.getKey());
					}
					catch (IOException c)
					{}
					break;
				case INTEGER:
					rf = new File(mPackageIntegerDir,new File(e.getKey()).getName());
					try
					{
						if (rf.exists())
							mRvals.add(new BufferedReader(new FileReader(rf)).readLine());
						else {
							mRvals.add(e.getValue());
							mRkeys.add(e.getKey());	
							break;
						}
						mRkeys.add("!"+e.getKey());
					}
					catch (IOException c)
					{}				
					break;
				default: mRvals.add(e.getValue()); mRkeys.add(e.getKey());
			}
		
		}
											
		mHeaderView.setText("No "+p1+" resources found.");			
		mListView.setAdapter(null);	
		// if no resources show message
		if ( res.isEmpty()){
			if ( mListView.getHeaderViewsCount()==0 )
				mListView.addHeaderView(mHeaderView);				
		} else
			mListView.removeHeaderView(mHeaderView);
					
		mAdapter = new ResourceListAdapter(ExperimentActivity.this, mRkeys, mRvals, mPackage.SOURCE);	
		mListView.setAdapter(mAdapter);
	}
	
	
	
	 
	private void mkDirs(){
		mPackageDir = Utils.mkDirs(getFilesDir(),"packages"); 
		mPackageDir = Utils.mkDirs(mPackageDir,mPackageName);		
		mPackageDrawableDir = Utils.mkDirs(mPackageDir,"drawable");		
		mPackageColorDir = Utils.mkDirs(mPackageDir,"color");		
		mPackageStringDir = Utils.mkDirs(mPackageDir,"string");		
		mPackageIntegerDir = Utils.mkDirs(mPackageDir,"integer");
		mPackageBooleanDir = Utils.mkDirs(mPackageDir,"boolean");		
			
	}
	
	
	private Runnable mapResources(){
		return new Runnable(){
			@Override
			public void run(){
				if ( isCached(mPackageName) ) {
					File cache = new File(getCacheDir(),mPackageName+".bin");
					try
					{
						FileInputStream fin = new FileInputStream(cache);
						ObjectInputStream in = new ObjectInputStream(fin);
						mRes = (AndroidPackage.Resources)in.readObject();
						in.close();
						fin.close();
					}
					catch (Exception e) {}				
				} else {
					mRes = mPackage.getResources();
				}
				runOnUiThread(mapResourcesCallback());
			}
		};
	}
	
	
	private Runnable mapResourcesCallback(){
		return new Runnable(){
			@Override
			public void run()
			{
				mProgress.dismiss();
				// TODO: do something after mapping rrsources here
				mHost.setCurrentTabByTag("string");
			}
		};
	}
	
	
	
	public synchronized void stopThread(Thread thread){
		if(thread!=null)
			thread = null;
	}
	

	
	
}
