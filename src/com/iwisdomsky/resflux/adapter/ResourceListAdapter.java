package com.iwisdomsky.resflux.adapter;

import android.app.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ResourceListAdapter extends ArrayAdapter<String> {

	private final Activity mContext;
	private final ArrayList<String> mResources;
	private final ArrayList<String> mValues; 
	private final String mSource;
	
	
	public static class ViewHolder {
		public TextView name;
		public TextView value;
		public ImageView icon;
	}

	
	public ResourceListAdapter(Activity context, ArrayList<String> resources, ArrayList<String> values, String source) {
		super (context, R.layout.resource_item, resources);
		this.mContext = context;	
		this.mResources = resources;
		this.mValues = values;
		this.mSource = source;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
		View rowView = convertView;

		// reuse views

		if ( rowView == null ) { 
			LayoutInflater inflater = mContext.getLayoutInflater(); 
			rowView = inflater.inflate(R.layout.resource_item, null);

			// configure view holder 
			ViewHolder viewHolder = new ViewHolder(); 
			viewHolder.name = (TextView) rowView.findViewById(R.id.res_name); 		
			viewHolder.value = (TextView) rowView.findViewById(R.id.res_value); 	
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.res_icon); 
			rowView.setTag(viewHolder); 
		}

		// fill data 
		ViewHolder holder = (ViewHolder) rowView.getTag(); 		
		holder.value.setText(mValues.get(position));
		if (mValues.get(position).toLowerCase().matches("^\\/?res\\/.*\\.png$") )
			try
			{
				ZipFile zFile = new ZipFile(mSource);
				ZipEntry entry = zFile.getEntry(mValues.get(position));
				InputStream in = zFile.getInputStream(entry);
				Drawable d = Drawable.createFromStream(in,"icon");
				holder.icon.setImageDrawable(d);
			}
			catch (IOException e){}
		else if (mValues.get(position).toLowerCase().matches("^\\/?.*\\.png$") ) {
			Drawable d = Drawable.createFromPath(mValues.get(position));
			holder.icon.setImageDrawable(d);
		} else if (mValues.get(position).toLowerCase().matches("^#[\\da-fA-F]{6,8}$") ) {
			ColorDrawable d = new ColorDrawable(Color.parseColor(mValues.get(position)));
			holder.icon.setImageDrawable(d);				
		} else
			holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.none));
	
		if ( mResources.get(position).startsWith("!") ) {
			String r = mResources.get(position).replaceFirst("!","");
			holder.name.setText(r);
			holder.value.setTextColor(0xFF77FF77);
		} else {
			holder.name.setText(mResources.get(position));
			holder.value.setTextColor(0xFFFFFFFF);
		}
	
		return rowView; 
	}
		
		
}
