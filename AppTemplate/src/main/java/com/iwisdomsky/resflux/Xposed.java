package com.iwisdomsky.resflux;


import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.*;
import java.io.*;
import java.util.*;
public class Xposed implements IXposedHookZygoteInit, IXposedHookInitPackageResources{

	private String mModulePackageName = "com.iwisdomsky.resflux";
	private ArrayList<String> mPackages;
	private File mPackagesDir;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPackages = new ArrayList<String>();
		mPackagesDir = new File("/data/data/"+mModulePackageName+"/files/packages");
		for ( File p : mPackagesDir.listFiles()) {
			mPackages.add(p.getName());
		}
		mPackages.remove("android");
		
		
		File pkg = new File(mPackagesDir,"android");
		if ( !pkg.exists() ) return;
		
		// for strings
		File strings_list = new File(pkg,"string");
		for ( File f : strings_list.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			XResources.setSystemWideReplacement("android", "string", f.getName(), br.readLine());
		}
		
		// for integers
		File ints_list = new File(pkg,"integer");
		for ( File f : ints_list.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			XResources.setSystemWideReplacement("android", "integer", f.getName(), Integer.parseInt(br.readLine()));
		}

		// for colors
		File colors_list = new File(pkg,"color");
		for ( File f : colors_list.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String l = br.readLine();
			XResources.setSystemWideReplacement("android", "color", f.getName(), Color.parseColor((l.startsWith("#")?"":"#")+l));
		}

		// for booleans
		File bools_list = new File(pkg,"boolean");
		for ( File f : bools_list.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			XResources.setSystemWideReplacement("android", "bool", f.getName(), Boolean.parseBoolean(br.readLine()));
		}


		// for drawables
		File drawables_list = new File(pkg,"drawable");
		for ( File f : drawables_list.listFiles()) {
			//BufferedReader br = new BufferedReader(new FileReader(f));
			final File d = f;
			if ( d.getName().matches("^.*\\.(png|PNG)$") )
				XResources.setSystemWideReplacement("android", "drawable", d.getName().replaceAll("^(.*)(\\.9)?\\.(png|PNG)$","$1"), new XResources.DrawableLoader(){
						public Drawable newDrawable(XResources p1, int p2) throws Throwable
						{
							Drawable e = Drawable.createFromPath(d.getAbsolutePath());
							return e;						
						}
				});
			else
				XResources.setSystemWideReplacement("android", "drawable", f.getName(), new XResources.DrawableLoader(){
						public Drawable newDrawable(XResources p1, int p2) throws Throwable
						{
							BufferedReader br = new BufferedReader(new FileReader(d));
							String l = br.readLine();
							Drawable e = new ColorDrawable(Color.parseColor((l.startsWith("#")?"":"#")+l));
							return e;						
						}
				});
		}
		
	}
	
	
	@Override
	public void handleInitPackageResources(InitPackageResourcesParam lpparam) throws Throwable
	{
		if(!mPackages.contains(lpparam.packageName))return;
		File pkg = new File(mPackagesDir,lpparam.packageName);
		
		// for strings
		File strings_list = new File(pkg,"string");
		for ( File f : strings_list.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			lpparam.res.setReplacement(lpparam.packageName,"string",f.getName(),br.readLine());
		}
		
		// for integers
		File ints_list = new File(pkg,"integer");
		for ( File f : ints_list.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			lpparam.res.setReplacement(lpparam.packageName,"integer",f.getName(),Integer.parseInt(br.readLine()));
		}
		
		// for colors
		File colors_list = new File(pkg,"color");
		for ( File f : colors_list.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String l = br.readLine();
			lpparam.res.setReplacement(lpparam.packageName,"color",f.getName(),Color.parseColor((l.startsWith("#")?"":"#")+l));
		}
		
		// for booleans
		File bools_list = new File(pkg,"boolean");
		for ( File f : bools_list.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			lpparam.res.setReplacement(lpparam.packageName,"bool",f.getName(),Boolean.parseBoolean(br.readLine()));
		}
		
		
		// for drawables
		File drawables_list = new File(pkg,"drawable");
		for ( File f : drawables_list.listFiles()) {
			//BufferedReader br = new BufferedReader(new FileReader(f));
			final File d = f;
			if ( d.getName().matches("^.*\\.(png|PNG)$") )
				lpparam.res.setReplacement(lpparam.packageName, "drawable", d.getName().replaceAll("^(.*)(\\.9)?\\.(png|PNG)$","$1"), new XResources.DrawableLoader(){
					public Drawable newDrawable(XResources p1, int p2) throws Throwable
					{
						Drawable e = Drawable.createFromPath(d.getAbsolutePath());
						return e;						
					}
				});
			else
				lpparam.res.setReplacement(lpparam.packageName, "drawable", f.getName(), new XResources.DrawableLoader(){
					public Drawable newDrawable(XResources p1, int p2) throws Throwable
					{
						BufferedReader br = new BufferedReader(new FileReader(d));
						String l = br.readLine();
						Drawable e = new ColorDrawable(Color.parseColor((l.startsWith("#")?"":"#")+l));
						return e;						
					}
				});
		}
	
		
		
		
	
	}

	

}
