package com.iwisdomsky.resflux;

import android.app.*;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.adapter.*;
import com.iwisdomsky.resflux.dialog.*;
import java.io.*;
import java.util.*;
import android.content.pm.*;

public class ExperimentActivity extends TabActivity implements TabHost.OnTabChangeListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
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
	private ArrayList<String> mRovals;
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
	
	//
	private int drawableIndex;

	
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
		mListView.setFastScrollEnabled(true);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mProgress.setOnCancelListener(new DialogInterface.OnCancelListener(){
			public void onCancel(DialogInterface p1)
			{
				mThread.stopX(ExperimentActivity.this);
				finish();
			}
		});
		
		mkDirs();
		
		((EditText)findViewById(R.id.filter)).addTextChangedListener(new TextWatcher(){
			public void beforeTextChanged(CharSequence a,int b,int c,int d){
				
			}
			public void onTextChanged(CharSequence a,int b,int c,int d){
				//mAdapter.getFilter().filter(a);
			}
			public void afterTextChanged(Editable e){

			}
		});
		
		
		// inform the user that first time will take some time
		if ( !isCached(mPackageName) ) {
			Toast t = Toast.makeText(this,"This process might take longer time to finish. Don't worry, this will only happen once, so please have some patience. :)",Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER,0,0);
			t.show();
		}
		
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
			d.setTitle(fkey);
			d.setColor(Color.parseColor(mRvals.get(p3)));
			d.setPositiveButton("Done", new SetColorDialog.OnClickListener(){
				public void onClick(String color)
				{		
					
					if ( !((color).equals(mRvals.get(i))) ){
						File copy_to = new File(mCurrentTab.equals("drawable")?mPackageDrawableDir:mPackageColorDir,new File(fkey).getName());
						InputStream in = new ByteArrayInputStream(color.getBytes());
						Utils.saveToFile(in,copy_to);	
						mRvals.set(i, color);
						String ukey = mRkeys.get(i).startsWith("!")?"":"!";
						mRkeys.set(i,ukey + mRkeys.get(i));
						mAdapter.notifyDataSetChanged();
					}
					Toast.makeText(ExperimentActivity.this,"REBOOT your phone to apply changes.",Toast.LENGTH_SHORT).show();
				}
			});
			d.setNegativeButton("Reset", new SetColorDialog.OnClickListener(){
				public void onClick(String color)
					{
						new File(mCurrentTab.equals("drawable")?mPackageDrawableDir:mPackageColorDir,new File(fkey).getName()).delete();
						resetValue(i);
					}
				});
			d.show();
		} else		
		// for PNG drawables 
		if ( mRvals.get(p3).matches("^.*\\.(PNG|png)$") ){
			drawableIndex = p3;
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_PICK);		
			startActivityForResult(Intent.createChooser(intent, "Select Replacement Drawable"), DRAWABLE_IMAGE_PICK);
			
		// for boolean	
		} else if ( mCurrentTab.equals("boolean") ) {
			SetBooleanDialog d = new SetBooleanDialog(this);
			d.setTitle(fkey);
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
						Toast.makeText(ExperimentActivity.this,"REBOOT your phone to apply changes.",Toast.LENGTH_SHORT).show();
					}
				});
			d.setNegativeButton("Reset", new SetBooleanDialog.OnClickListener(){
					public void onClick(boolean value)
					{
						new File(mPackageBooleanDir,new File(fkey).getName()).delete();
						resetValue(i);
					}
				});
			d.show();
		// if string or integer
		} else {
			SetTextDialog d = new SetTextDialog(this);
			d.setTitle(fkey);
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
						Toast.makeText(ExperimentActivity.this,"REBOOT your phone to apply changes.",Toast.LENGTH_SHORT).show();
					}
				});
			d.setNegativeButton("Reset", new SetTextDialog.OnClickListener(){
					public void onClick(String text)
					{
						new File(mCurrentTab.equals("string")?mPackageStringDir:mPackageIntegerDir,new File(fkey).getName()).delete();
						resetValue(i);
					}
				});
			d.show();	
		}
	}

	
	
	// resets a modified resource
	public void resetValue(int i){
		mRvals.set(i,mRovals.get(i));
		mRkeys.set(i,mRkeys.get(i).replaceFirst("!",""));
		mAdapter.notifyDataSetChanged();
	}
	
	
	// long press to clear
	@Override
	public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		
		String key = mRkeys.get(p3);
		if ( key.startsWith("!") ) {
			key = key.replaceFirst("!","");
		}
		
		switch (Constants.ResourceTypes.valueOf(mCurrentTab.toUpperCase()) ) {
			case DRAWABLE: 
				new File(mPackageDrawableDir,new File(mRvals.get(p3)).getName()).delete();
				new File(mPackageDrawableDir,new File(key).getName()).delete();	
				break;
			case STRING:
				new File(mPackageStringDir,new File(key).getName()).delete();
				break;
			case INTEGER:
				new File(mPackageIntegerDir,new File(key).getName()).delete();
				break;
			case COLOR:
				new File(mPackageColorDir,new File(key).getName()).delete();
				break;
			case BOOLEAN:
				new File(mPackageBooleanDir,new File(key).getName()).delete();			
				break;
		}
		resetValue(p3);
		
		Toast.makeText(this,"Original value restored!\nREBOOT your phone to apply changes.",Toast.LENGTH_SHORT).show();
		return true;
	}
	
	
	
	private static final int DRAWABLE_IMAGE_PICK = 1;

	@Override 
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(requestCode == DRAWABLE_IMAGE_PICK && data != null && data.getData() !=null) {
			Uri _uri = data.getData();
			//User had pick an image.
			Cursor cursor = getContentResolver().query(_uri, new String[]{ android.provider.MediaStore.Images.ImageColumns.DATA}, null,null, null);
			cursor.moveToFirst();
			final String img = cursor.getString(0);cursor.close();
			
			if ( img.toLowerCase().endsWith(".png") ) {
				File copy_to = new File(mPackageDrawableDir,new File(mRvals.get(drawableIndex)).getName());
				try
				{
					FileInputStream in = new FileInputStream(new File(img));
					Utils.saveToFile(in, copy_to);
				}
				catch (FileNotFoundException e)
				{}

				mRvals.set(drawableIndex, copy_to.getAbsolutePath());
				String ukey = mRkeys.get(drawableIndex).startsWith("!")?"":"!";
				mRkeys.set(drawableIndex,ukey + mRkeys.get(drawableIndex));
				mAdapter.notifyDataSetChanged();
				Toast.makeText(ExperimentActivity.this,"REBOOT your phone to apply changes.",Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(this,"Invalid image file format! Only PNG image files are accepted.",Toast.LENGTH_SHORT).show();
			
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onKeyDown(int c, KeyEvent e) {
		if ( c==e.KEYCODE_MENU && e.getAction()==e.ACTION_DOWN ) {
			StringBuilder sb = new StringBuilder();
		
			try
			{
				sb.append("<b>App Name</b><br />");		
				ApplicationInfo info = getPackageManager().getApplicationInfo(mPackageName, 0);
				sb.append(info.loadLabel(getPackageManager())+"<br><br>");
				sb.append("<b>Location</b><br>");
				sb.append(info.sourceDir+"<br><br>");
				sb.append("<b>Package Name</b><br>");
				sb.append(mPackageName+"<br><br>");
			
				new AlertDialog.Builder(this)
				.setIcon(info.loadIcon(getPackageManager()))
				.setTitle("Properties")
				.setMessage(Html.fromHtml(sb.toString()))
				.create()
				.show();
			}
			catch (PackageManager.NameNotFoundException z){}
		
			
			
		
		}
		return super.onKeyDown(c,e);
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
		mRovals = new ArrayList<String>();	
		
		// transfer keys and values into separate arraylists
		for (Map.Entry<String, String> e : res) {		
			File rf = null;
			File rfc = null;
			mRovals.add(e.getValue());
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
							String cl = new BufferedReader(new FileReader(rfc)).readLine();
							mRvals.add((cl.startsWith("#")?"":"#")+cl);				
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
						if (rf.exists()) {
							String color = new BufferedReader(new FileReader(rf)).readLine();
							mRvals.add((color.startsWith("#")?"":"#")+color);
						} else {
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
		getCacheDir().mkdir();
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
				try {
					mHost.setCurrentTabByTag("string");
				} catch (Exception e) {}
			}
		};
	}
	
	
	
	public synchronized void stopThread(Thread thread){
		if(thread!=null)
			thread = null;
	}
	

	
	
}
