package com.iwisdomsky.resflux;

import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import java.io.*;
import java.util.*;

public class AndroidPackage
{
	
	public final String LABEL; 
	public final String PACKAGE_NAME;
	public final Drawable ICON;
	public final String SOURCE;
	private final Context mContext;
		
	public AndroidPackage(Context ctx,String package_name){
		this.mContext = ctx; 		
		PackageManager pm = ctx.getPackageManager();
		ApplicationInfo app = null;
		try
		{
			app = pm.getApplicationInfo(package_name, 0);			
		}
		catch (PackageManager.NameNotFoundException e)
		{}
		
		this.LABEL = app.loadLabel(pm).toString();
		this.PACKAGE_NAME = app.packageName;
		this.ICON = app.loadIcon(pm);
		this.SOURCE = app.sourceDir;
					
	}
	
	
	
	public AndroidPackage.Resources getResources(){
		DataInputStream in = AaptManager.getDumpResourcesStream(mContext, this.SOURCE);
		TreeMap<String,String> drawables = new TreeMap<String,String>()/*getResourcesOfType("drawable")*/;
		TreeMap<String,String> strings =  new TreeMap<String,String>()/*/*getResourcesOfType("string")*/;
		TreeMap<String,String> colors =  new TreeMap<String,String>()/*/*getResourcesOfType("color")*/;
		TreeMap<String,String> bools =  new TreeMap<String,String>()/*/*getResourcesOfType("bool")*/;
		TreeMap<String,String> integers =  new TreeMap<String,String>()/*/*getResourcesOfType("integer")*/;
				
		String  line,
				res_pattern ="^resource.+:(.+)/([\\w\\d]+):.*$",
			    val_pattern = "^\\([^\\s]+\\)\\s[\\\"]?([^\\\"]+)[\\\"]?$";
		try
		{
			while ( (line = in.readLine()) != null )
			{
				line = line.trim();
				if ( line.matches(res_pattern) ) {
					String  i_val, // value
							i_res = line.replaceAll(res_pattern, "$2"), // reaource name
							res_type = line.replaceAll(res_pattern, "$1"); // resource type
							
					line = in.readLine().trim();
					if ( line.matches(val_pattern) ) 
						i_val = line.replaceAll(val_pattern,"$1");
					else
						i_val = "";
												
					try{
					switch ( ResType.valueOf(res_type.toUpperCase()) ) {
						
						// for DRAWABLES
						case DRAWABLE:
							// block xml drawables as they are not supported yet
							if ( i_val.endsWith("xml") )
								continue;			
							drawables.put(i_res,i_val);
							break;
							
							
						// for STRINGS	
						case STRING:					
							strings.put(i_res,i_val);						
							break;
							
							
						// for COLORS	
						case COLOR:
							// block xml colors as they are not supported yet
							if ( !i_val.matches("^#([a-fA-F\\d]{6}|[a-fA-F\\d]{8})$") )
								continue;
							colors.put(i_res,i_val);	
							break;
							
							
						// for BOOLEANS	
						case BOOL:			
							// convert boolean hex value into true/false where #fffffff = true and #00000000 = false
							if ( i_val.matches("^#[f]+$") )
								i_val = "true";
							else if ( i_val.matches("^#[0]+$") )
								i_val = "false";
							else
								continue;
							bools.put(i_res,i_val);
							break;
							
							
						// for INTEGERS	
						case INTEGER:				
							// since integer values are in its hex form, let's convert it into dec
							if ( i_val.matches("^#[a-fA-F\\d]+$") )
								i_val = String.valueOf(Integer.parseInt(i_val.replaceAll("^#([a-fA-F\\d]+)$","$1"),16));
							else
								continue;
							integers.put(i_res,i_val);
							break;
							
							
						default:continue;
					}
					} catch (IllegalArgumentException e) {}
					
					
								
				}
			}
		}
		catch (IOException e)
		{}
		return new AndroidPackage.Resources(drawables, strings, colors, bools, integers);		
	}

	// Resources holder
	public static class Resources implements Serializable{
		public final TreeMap<String,String> DRAWABLES;
		public final TreeMap<String,String> STRINGS;	
		public final TreeMap<String,String> COLORS;
		public final TreeMap<String,String> BOOLEANS;
		public final TreeMap<String,String> INTEGERS;
	
		public Resources(TreeMap<String,String> drawable_resources_list, TreeMap<String,String> string_resources_list, TreeMap<String,String> color_resources_list, TreeMap<String,String> boolean_resources_list, TreeMap<String,String> integer_resources_list){
			this.DRAWABLES = drawable_resources_list;
			this.STRINGS = string_resources_list;
			this.COLORS = color_resources_list;
			this.BOOLEANS = boolean_resources_list;
			this.INTEGERS = integer_resources_list;
		}
		
	}

	private static enum ResType{
		DRAWABLE,STRING,COLOR,BOOL,INTEGER;
	}
	

	
	
}
