package com.iwisdomsky.resflux.adapter;

import android.app.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;
import java.io.*;
import java.util.*;

public class FileListAdapter extends ArrayAdapter<File>
 {

	private final Activity mContext;
	private final ArrayList<File> mFiles;


	static class ViewHolder {
		public TextView name;
		public ImageView icon;
	}


	public FileListAdapter(Activity context, ArrayList<File> files) {
		super (context, R.layout.file_item, files);
		this.mContext = context;	
		this.mFiles = files;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
		View rowView = convertView;

		// reuse views

		if ( rowView == null ) { 
			LayoutInflater inflater = mContext.getLayoutInflater(); 
			rowView = inflater.inflate(R.layout.file_item, null);

			// configure view holder 
			ViewHolder viewHolder = new ViewHolder(); 
			viewHolder.name = (TextView) rowView.findViewById(R.id.file_name); 		 	
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.file_icon); 
			rowView.setTag(viewHolder); 
		}

		// fill data 
		ViewHolder holder = (ViewHolder) rowView.getTag(); 	
		holder.name.setText(mFiles.get(position).getName());
		/*if (mFiles.get(position).isDirectory()) {
			holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.folder));
			rowView.setBackgroundResource(R.drawable.button_1);
		} else {
			Drawable d = Drawable.createFromPath(mFiles.get(position).getAbsolutePath());
			holder.icon.setImageDrawable(d);
			rowView.setBackgroundResource(R.drawable.button_2);
		}
		
		if( mFiles.size()-1==position ) {
			holder.name.setText("Go back");
			rowView.setBackgroundResource(R.drawable.button_4);
		}
		*/
		return rowView; 
	}


}
