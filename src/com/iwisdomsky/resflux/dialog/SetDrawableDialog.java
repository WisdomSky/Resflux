package com.iwisdomsky.resflux.dialog;


import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;
import com.iwisdomsky.resflux.adapter.*;
import java.io.*;
import java.util.*;

public class SetDrawableDialog
{

	private final Context mContext;
	private AlertDialog mCPD;
	private final AlertDialog.Builder mDialog;
	private final ArrayList<File> mFiles;
	private final ListView mListView;
	private final FileListAdapter mAdapter; 
	private final Button mButton;
	private final Button mCButton;
	
	
	public SetDrawableDialog(Context context){
		this.mContext = context;
		this.mDialog = new AlertDialog.Builder(mContext);
				
		// Vertical LinearLayout
		LinearLayout ll = new LinearLayout(mContext);
		ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setPadding(5,0,5,5);
		ll.setMinimumWidth(400);
		
		
		// create the list view
		mListView = new ListView(mContext);
		/*mListView.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 0, 1f));
		
		*/// initialize the list
		mFiles = new ArrayList<File>();
		
		/*// populate the list
	 	File ex = Environment.getExternalStorageDirectory();
		populateList(ex.listFiles());

		// add back button
		mFiles.add(new File("/"));
		*/// create the adapter
		mAdapter = new FileListAdapter((Activity)mContext,mFiles);
		// set the adapter
		//mListView.setAdapter(mAdapter);

		
		LinearLayout vll = new LinearLayout(mContext);
		vll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		vll.setOrientation(LinearLayout.HORIZONTAL);

	
		// Done Button
	 	mButton = new Button(mContext);
		mButton.setLayoutParams(new TableLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		mButton.setVisibility(View.GONE);
		mButton.setGravity(Gravity.CENTER);		
		mButton.setBackgroundResource(R.drawable.button_2);

		// Cancel Button
		mCButton  = new Button(mContext);
		mCButton.setLayoutParams(new TableLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		mCButton.setVisibility(View.GONE);
		mCButton.setGravity(Gravity.CENTER);		
		mCButton.setBackgroundResource(R.drawable.button_1);
		
		
		// Adding Views
		
		ll.addView(new SDTextView(mContext,"Select image:"));	
		//ll.addView(mListView);
		vll.addView(mButton);
		vll.addView(mCButton);
		ll.addView(vll);
		// Set the view into the Dialog
		mDialog.setView(ll);
	}




	public SetDrawableDialog setOnItemClickListener(final SetDrawableDialog.OnItemClickListener listener){					
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
			{			
				
				// get current selected file
				File f = mFiles.get(p3);
				// check if it is a directory
				if ( f.isDirectory() ) {
					// clear the list first before we add the new files
					mFiles.clear();	
					// if it is a directory, reload the listview 
					// with the files list of the selected directory
					// but first, check files permissions
					try {
						populateList(f.listFiles());
					} catch ( Exception e ) {
						mFiles.clear();
					}
					// add back button (up one level)
					if ( !f.equals(new File("/")) )
						// add the parent dir
						mFiles.add(f.getParentFile());
					else
						mFiles.add(new File("/"));
					// update the list view with the new list	
					mAdapter.notifyDataSetChanged();
					
				} else {
					// add the listener
					listener.onItemClick(f);
					mCPD.dismiss();
				}
				
			
			}
		});
		return this;		
	}

	private void populateList(File[] f){
		// start populating
		for (File sf : f) {
			// filter results	
			if ( sf.getName().matches("^[^\\.].*(png|PNG)$") || (sf.isDirectory() && !sf.getName().startsWith(".") )) {		
				mFiles.add(sf);
			}
		}
		// sort the list
		Collections.sort(mFiles, new Comparator<File>(){
				public int compare(File p1, File p2)
				{					
					return p1.getName().compareToIgnoreCase(p2.getName());
				}
			});
	}

	@Override
	public void show(){
		// create the dialog
		mCPD = mDialog.create();
		// show the dialog
		mCPD.show();
	}

	
	public SetDrawableDialog setPositiveButton(CharSequence text,final SetDrawableDialog.OnClickListener listener){					
		mButton.setText(text);
		mButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View p1)
				{
					if ( listener!=null ) 
						listener.onClick();
					mCPD.dismiss();
				}
			});
		mButton.setVisibility(View.VISIBLE);
		return this;		
	}


	public SetDrawableDialog setNegativeButton(CharSequence text,final SetDrawableDialog.OnClickListener listener){					
		mCButton.setText(text);
		mCButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View p1)
				{
					if ( listener!=null )
						listener.onClick();
					mCPD.dismiss();
				}
			});
		mCButton.setVisibility(View.VISIBLE);
		return this;		
	}
	
	
	

	public static interface OnItemClickListener {
		public void onItemClick(File file); 
	}
	public static interface OnClickListener {
		public void onClick(); 
	}
	
	private static class SDTextView extends TextView{
		public SDTextView(Context context,String text){
			super(context);
			setText(text);
			setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		}
	}
	

}
