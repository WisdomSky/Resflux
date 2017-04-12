package com.iwisdomsky.resflux;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.iwisdomsky.resflux.infoflow.ARSCFileParser;

import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;

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
		TreeMap<String,String> drawables = new TreeMap<String,String>()/*getResourcesOfType("drawable")*/;
		TreeMap<String,String> strings =  new TreeMap<String,String>()/*/*getResourcesOfType("string")*/;
		TreeMap<String,String> colors =  new TreeMap<String,String>()/*/*getResourcesOfType("color")*/;
		TreeMap<String,String> bools =  new TreeMap<String,String>()/*/*getResourcesOfType("bool")*/;
		TreeMap<String,String> integers =  new TreeMap<String,String>()/*/*getResourcesOfType("integer")*/;


		ARSCFileParser parser = new ARSCFileParser();
		try {
			parser.parse(this.SOURCE);
			for ( ARSCFileParser.ResPackage pkg : parser.getPackages()) {
				Log.v("apk-parser", "--------" + pkg.getPackageName() + "--------");
				for ( ARSCFileParser.ResType type : pkg.getDeclaredTypes()) {
					Log.v("apk-parser", "TYPE: "+ type.getId() +":" +type.getTypeName()+" "+ type.getAllResources().size());

					for (ARSCFileParser.AbstractResource resource : type.getAllResources()) {

						if (type.getTypeName().equals("color") && resource instanceof ARSCFileParser.ColorResource) {
							ARSCFileParser.ColorResource str = (ARSCFileParser.ColorResource) resource;

							colors.put(resource.getResourceName(), str.getHexARGB());

							Log.v("apk-parser", type.getTypeName() + ": " + resource.getResourceName() + " - "+str.getHexARGB());
						} else
						if (type.getTypeName().equals("string") && resource instanceof ARSCFileParser.StringResource) {
							ARSCFileParser.StringResource str = (ARSCFileParser.StringResource) resource;

							strings.put(resource.getResourceName(),str.getValue());

							Log.v("apk-parser", type.getTypeName() + ": " + resource.getResourceName() + " - "+str.getValue());
						} else
						if (type.getTypeName().equals("integer") && resource instanceof ARSCFileParser.IntegerResource) {
							ARSCFileParser.IntegerResource str = (ARSCFileParser.IntegerResource) resource;

							integers.put(resource.getResourceName(), String.valueOf(str.getValue()));

							Log.v("apk-parser", type.getTypeName() + ": " + resource.getResourceName() + " - "+str.getValue());
						} else
						if (type.getTypeName().equals("bool") && resource instanceof ARSCFileParser.BooleanResource) {
							ARSCFileParser.BooleanResource str = (ARSCFileParser.BooleanResource) resource;

							bools.put(resource.getResourceName(),str.getValue() ? "true" : "false");

							Log.v("apk-parser", type.getTypeName() + ": " + resource.getResourceName() + " - "+str.getValue());
						} else if (type.getTypeName().equals("drawable") && resource instanceof ARSCFileParser.StringResource) {
							ARSCFileParser.StringResource str = (ARSCFileParser.StringResource) resource;

							if (!str.getValue().endsWith(".png")) continue;

							drawables.put(resource.getResourceName(), str.getValue());

							Log.v("apk-parser", type.getTypeName() + ": " + resource.getResourceName() + " - " + str.getValue());

						}

					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


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
