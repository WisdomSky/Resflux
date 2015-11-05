package com.iwisdomsky.resflux;

import android.content.*;
import android.util.Log;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class AaptManager 
{
	private static List<String> getDump(Context context, String apk_path,String what)
	{

		String rand = new BigInteger(130, new SecureRandom()).toString(32);
		File target = new File(context.getFilesDir(), "apk"+ rand +".apk");

		try {
			File destination = Utils.mkDirs(context.getFilesDir(), "opt");
			AaptManager.unzip(context, new File(apk_path), destination);
			AaptManager.recursiveDelete(new File(destination, "res"));
			AaptManager.recursiveDelete(new File(destination,"assets"));
			AaptManager.recursiveDelete(new File(destination,"META-INF"));
			AaptManager.zip(destination.toString(), target);
			AaptManager.recursiveDelete(destination);
		} catch (IOException e) {
			e.printStackTrace();
		}


		try {

			MultipartUtility multipart = new MultipartUtility(Constants.SERVER_URL, "UTF-8");
			multipart.addFilePart("apk", target);
			List<String> response = multipart.finish(); // response from server.
			target.delete();
			return response;

		} catch (Exception e) {

		}

		return null;
	}




	public static void unzip(Context ctx, File zipFile, File destination) throws IOException {

		ZipInputStream zis = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(zipFile)));
		try {
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[8192];
			while ((ze = zis.getNextEntry()) != null) {
				File file = new File(destination, ze.getName());
				File dir = ze.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs())
					throw new FileNotFoundException("Failed to ensure directory: " +
							dir.getAbsolutePath());
				if (ze.isDirectory())
					continue;
				FileOutputStream fout = new FileOutputStream(file);
				try {
					while ((count = zis.read(buffer)) != -1)
						fout.write(buffer, 0, count);
				} finally {
					fout.close();
				}
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
			}
		} finally {
			zis.close();
		}


	}


	public static void recursiveDelete(final File file){
		if ( file.isDirectory() )
			for ( final File sub : file.listFiles() )
				recursiveDelete(sub);
		file.delete();

	}

	public static void zip(String inputFolderPath, File outZipPath) {
		try {
			FileOutputStream fos = new FileOutputStream(outZipPath);
			ZipOutputStream zos = new ZipOutputStream(fos);
			File srcFile = new File(inputFolderPath);
			File[] files = srcFile.listFiles();
			Log.d("", "Zip directory: " + srcFile.getName());
			for (int i = 0; i < files.length; i++) {
				Log.d("", "Adding file: " + files[i].getName());
				byte[] buffer = new byte[1024];
				FileInputStream fis = new FileInputStream(files[i]);
				zos.putNextEntry(new ZipEntry(files[i].getName()));
				int length;
				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
				fis.close();
			}
			zos.close();
			fos.close();
			outZipPath.setReadable(true,false);
		} catch (IOException ioe) {
			Log.e("", ioe.getMessage());
		}
	}


	public static List<String> getDumpResourcesStream(Context context, String apk_path){
		return getDump(context, apk_path, "resources");
	}

}
