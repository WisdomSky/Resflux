package com.iwisdomsky.resflux.adapter;


import android.app.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;
import java.util.*;

public class PackageListAdapter extends ArrayAdapter<String> {

	private final Activity mContext;
	public final ArrayList<String> mPackages;
	
	
	static class ViewHolder {
		public TextView name;
		public TextView source;
		public ImageView icon;
	}

	
	public PackageListAdapter(Activity context, ArrayList<String> packages) {
		super (context, R.layout.package_item, packages);
		this.mContext = context;	
		this.mPackages = packages;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
		View rowView = convertView;

		// reuse views

		if ( rowView == null ) { 
			LayoutInflater inflater = mContext.getLayoutInflater(); 
			rowView = inflater.inflate(R.layout.package_item, null);

			// configure view holder 
			ViewHolder viewHolder = new ViewHolder(); 
			viewHolder.name = (TextView) rowView.findViewById(R.id.app_name); 		
			viewHolder.source = (TextView) rowView.findViewById(R.id.app_source); 	
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.app_icon); 
			rowView.setTag(viewHolder); 
		}

		// fill data 
		ViewHolder holder = (ViewHolder) rowView.getTag(); 
		AndroidPackage pkg = new AndroidPackage(mContext,mPackages.get(position));
		
		// fill holder with data
		holder.name.setText(pkg.LABEL);
		holder.source.setText(pkg.SOURCE);
		holder.icon.setImageDrawable(pkg.ICON);
		
		// color coding red = system, green = phone, yellow = sdcard
		if ( pkg.SOURCE.startsWith("/system") ) 
			holder.source.setTextColor(0xFFFF7777);
		else if ( pkg.SOURCE.startsWith("/data") ) 
			holder.source.setTextColor(0xFF77FF77);
		else
			holder.source.setTextColor(0xFFFFFF77);

		return rowView; 
	}
		
		
}
