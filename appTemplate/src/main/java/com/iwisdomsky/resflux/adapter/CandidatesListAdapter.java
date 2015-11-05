package com.iwisdomsky.resflux.adapter;


import android.app.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class CandidatesListAdapter extends ArrayAdapter<String> {

	private final Activity mContext;
	public final ArrayList<String> mCandidates;


	static class ViewHolder {
		public TextView name;
		public TextView source;
		public ImageView icon;
	}


	public CandidatesListAdapter(Activity context, ArrayList<String> candidates) {
		super (context, R.layout.candidate_item, candidates);
		this.mContext = context;	
		this.mCandidates = candidates;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
		View rowView = convertView;

		// reuse views

		if ( rowView == null ) { 
			LayoutInflater inflater = mContext.getLayoutInflater(); 
			rowView = inflater.inflate(R.layout.candidate_item, null);

			// configure view holder 
			ViewHolder viewHolder = new ViewHolder(); 
			viewHolder.name = (TextView) rowView.findViewById(R.id.can_name); 		
			viewHolder.source = (TextView) rowView.findViewById(R.id.can_source); 	
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.can_icon); 
			rowView.setTag(viewHolder); 
		}

		// fill data 
		ViewHolder holder = (ViewHolder) rowView.getTag(); 
		File f = new File(mCandidates.get(position));
		try
		{
			String line,label=null,desc=null,icon=null,
				   pattern = "^([\\s\\.\\w0-9]+)(=|:)\\s*\"?([^\"]+)\"?$";
			DataInputStream in = new DataInputStream(new ZipFile(f).getInputStream(new ZipFile(f).getEntry("Resflux.ini")));
			while ( (line = in.readLine()) != null) {
				line = line.trim();
				if ( line.matches(pattern) ){
					String key = line.replaceAll(pattern,"$1").replaceAll("\\s","");
					String val = line.replaceAll(pattern,"$3").trim();
					if ( key.equals("resflux.label") )
						label = val;
					else if ( key.equals("resflux.desc") )
						desc = val;
					else if ( key.equals("resflux.icon") )
						icon  = val;
					if ( !key.startsWith("resflux") )
						break;
				}					
			}
			
			
			holder.name.setText(label==null?f.getName():label);
			holder.source.setText(desc==null?f.getAbsolutePath():desc);
			
			Drawable d;
			if ( icon!=null ) {
				try {
					InputStream icon_in = new ZipFile(f).getInputStream(new ZipFile(f).getEntry(icon));
					d = Drawable.createFromStream(icon_in,"icon");
				} catch ( Exception e ) {
					d = mContext.getResources().getDrawable(R.drawable.none);	
				}
			} else {
				d = mContext.getResources().getDrawable(R.drawable.none);
			}
			holder.icon.setImageDrawable(d);
			
		}
		catch (IOException e)
		{}

		return rowView; 
	}


}
