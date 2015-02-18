package com.iwisdomsky.resflux;

import android.content.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;

public class Engine
{
	private Context mContext;
	private ZipFile mFile;
	
	public Engine(Context context){
		this.mContext = context;
	}
	
	public Engine(Context context, File zip_file){
		this.mContext = context;
		try
		{
			this.mFile = new ZipFile(zip_file);
		}
		catch (IOException e)
		{}
	}
	
	public Engine(Context context, ZipFile zip_file){
		this.mContext = context;
		this.mFile = zip_file;
	}
	
	public void setZipFile(File zip_file){
		try
		{
			this.mFile = new ZipFile(zip_file);
		}
		catch (IOException e)
		{} 
	}

	public void setZipFile(ZipFile zip_file){
		this.mFile = zip_file;
	}
	

	
	
	public void start(){
		File base_dir = new File(mContext.getFilesDir(),"packages");
		DataInputStream script_stream = new DataInputStream(getResfluxIniStream());
		String stmt;
		File pkg_dir = null;
		try
		{
			while ((stmt = script_stream.readLine()) != null)
			{
				// if comment or empty
				if ( stmt.matches("^(#|;).*$") || stmt.isEmpty() )
				 	continue;
				// set package name
				stmt = stmt.trim();
				if ( stmt.startsWith("[") ) {
					pkg_dir = Utils.mkDirs(base_dir,stmt.replaceAll("^\\[([\\s\\.\\da-zA-Z_]+)\\]$","$1").replaceAll("\\s",""));				
					continue;
				}
				// ini
				if ( stmt.startsWith("ini") ) {
					String  pattern = "^ini\\s*\\.([\\s\\.\\w0-9]+)(=|:)\\s*\"?([^\"]+)\"?$",
							method = stmt.replaceAll(pattern,"$1").replaceAll("\\s",""),
							value = stmt.replaceAll(pattern,"$3");
					ini(method,value);				
					continue;
				}
				// key-value
				String 	pattern = "^(drawable|string|color|integer|boolean|layout)\\s*\\.([\\s\\.\\w0-9]+)(=|:)\\s*\"?([^\"]+)\"?$";
				if ( stmt.matches(pattern) ) {				
					String type = stmt.replaceAll(pattern,"$1").replaceAll("\\s",""),
						   res = stmt.replaceAll(pattern,"$2").replaceAll("\\s",""),
						   val = stmt.replaceAll(pattern,"$4");
					File res_dir = Utils.mkDirs(pkg_dir,type);
					// if png drawable
					if ( type.equals("drawable") && val.endsWith(".png") ) {
						InputStream in = mFile.getInputStream(new ZipEntry(val));	
						File out = new File(res_dir,res+".png");
						Utils.saveToFile(in,out);
					// else
					} else {
						File out = new File(res_dir,res);
						ByteArrayInputStream in = new ByteArrayInputStream(val.getBytes());
						Utils.saveToFile(in,out);
					}
				}
			}
		}
		catch (IOException e)
		{}
	}
	

	
	
	
	private InputStream getResfluxIniStream(){
		try
		{
			return mFile.getInputStream(new ZipEntry("Resflux.ini"));
		}
		catch (IOException e)
		{}
		return null;
	}
	
	public static String getFirstError(File file){
		InputStream script_stream = null;
		try
		{
			script_stream = new ZipFile(file).getInputStream(new ZipEntry("Resflux.ini"));
		}
		catch (IOException e)
		{}

		String  stmt,
				// key-val statement matcher
				pattern1 = "^(drawable|string|color|integer|boolean|layout)\\s*\\.[\\s\\.\\w0-9]+(=|:)\\s*\"?([^\"]+)\"?$",
				// comment matcher
				pattern2 = "^(#|;).*$",
				// section matcher
				pattern3 = "^\\[[\\s\\.\\da-zA-Z]+\\]$",
				// info statement matcher
				pattern4 =  "^(resflux\\s*\\.[\\s\\.\\w0-9]+)(=|:)\\s*\"?([^\"]+)\"?$",
				// ini statement matcher
				pattern5 = "^(ini\\s*\\.[\\s\\.\\w0-9]+)(=|:)\\s*\"?([^\"]+)\"?$";
		// we use the check_point to determine the current location of a statement within the script
		// we use the check_point to enforce rules in proper arrangement of statements
		int check_point = 0, line = 0;		
		boolean isPkgDeclared = false;
		DataInputStream in = new DataInputStream(script_stream);
		try
		{
			while ((stmt = in.readLine()) != null)
			{
				// strip white spaces
				stmt = stmt.trim();
				// set current line
				line++;
				// check if the line matches any of the patterns
				if (  stmt.isEmpty()
				   || stmt.matches(pattern1)
				   || stmt.matches(pattern2)
				   || stmt.matches(pattern3)
				   || stmt.matches(pattern4)
				   || stmt.matches(pattern5) ) {
					   // check if rules are applied
					   
					/* RULE 1: 
					* All resflux.* statements must be declared
					* before any other statements and sections
					*/        
					if ( stmt.startsWith("resflux") ) {
						if ( check_point > 1 )
						   return "Line "+line+": Error Status 1";							   
				   		else
							check_point=1;
						continue;
					}
				   /* RULE 2: 
					* All ini.* statements must be declared
					* after resflux.* statements and before any other statements and sections
					*/  
					if ( stmt.startsWith("ini") ) {
						if ( check_point<3  )
						   check_point = 2;
						else 
							return "Line "+line+": Error Status 2";
				   		continue;
					}
				   	// section
					if ( stmt.startsWith("[") ) {
							check_point = 3;
							isPkgDeclared = true;
							continue;
					}
					/* RULE 3: 
					* Before declaring drawable.*, string.*, color.*, boolean.* and integer.* statements
					* a section must be defined atleast once.
					*/  
					if ( stmt.matches("^(drawable|string|layout|color|integer|boolean).+$") ) {
						if ( !isPkgDeclared )
							return "Line "+line+": Error Status 3";		
					   String restype = stmt.replaceAll(pattern1,"$1"),
					   		  resval = stmt.replaceAll(pattern1,"$3");	
						// verify value format
						switch ( Constants.ResourceTypes.valueOf(restype.toUpperCase()) ) {
							case DRAWABLE:
								if ( !(resval.matches("^#?([a-fA-Z\\d]{6}|[a-fA-F\\d]{8})$") || stmt.matches("^.*\\.png$")) )
									return "Line "+line+": Error Status 4";
								break;
							case COLOR:
							  	if ( !resval.matches("^#?([a-fA-Z\\d]{6}|[a-fA-F\\d]{8})$") )
								   	return "Line "+line+": Error Status 4";							   
								break;
						   case BOOLEAN:
							   if ( !resval.matches("^(true|false)$") )
								   return "Line "+line+": Error Status 4";							   
							   break;
						   case INTEGER:
							   if ( !resval.matches("^[\\d]+$") )
								   return "Line "+line+": Error Status 4";							   
							   break;
						}
						check_point=3;
					}				
				} else {
					return "Line "+line+": Error Status 0. ";
				}
			}
		}
		catch (IOException e)
		{}
		return null;
	}
	
	
	private void ini(String mn, String v){
		try
		{
			while ( mn.contains("_") ) {
				int i = mn.indexOf("_");
				mn = mn.replaceFirst("_","");
				StringBuilder sb = new StringBuilder();
				for (char c : mn.toCharArray()) {
					sb.append(c);
				}
				Character z = sb.charAt(i);
				sb.deleteCharAt(i);
				sb.insert(i,z.toString().toUpperCase());
				z = sb.charAt(0);
				sb.deleteCharAt(0);
				sb.insert(0,z.toString().toUpperCase());	
				mn=sb.toString();			
			}
			Method m = this.getClass().getMethod("ini"+mn,new Class[]{String.class});
			m.invoke(this,new Object[]{v});
		}
		catch (final NoSuchMethodException e) {	
		} catch ( Exception e) {}
	}
	
	/*
	*
	*
	*/
	public void iniMagicDir(String dir_name){
		File dest = Utils.mkDirs(mContext.getFilesDir(),"packages");	
		Enumeration entries = mFile.entries();
		while ( entries.hasMoreElements() ) {
			ZipEntry ze = (ZipEntry)entries.nextElement();			
			if ( ze.getName().startsWith(dir_name+"/") ) {
				final String dir = new File(ze.getName().replaceFirst(dir_name+"/","")).getParent();
				File fdir = dest;
				if ( dir.contains("/") ) {
					String[] dirs = dir.split("/");
					for (String q : dirs) {
						fdir = Utils.mkDirs(fdir,q);
					}
				} else {
					fdir = Utils.mkDirs(fdir,dir);
				}					
				File f = new File(fdir,new File(ze.getName()).getName());		
					try
					{
						InputStream in = mFile.getInputStream(ze);
						Utils.saveToFile(in,f);	
					}
					catch (IOException e)
					{} 
				f.setReadable(true,false);
				f.setExecutable(true,false);
			}
		}

	}
	
	
	
	
	
}
