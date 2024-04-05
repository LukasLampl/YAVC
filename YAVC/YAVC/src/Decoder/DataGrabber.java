package Decoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataGrabber {
	private String content = "";
	private File cache = null;
	
	/*
	 * Purpose: Resolve the deflate the YAVC file
	 * Return Type: String => Content of the YAVC file (CAN GET REALLY REALLY HUGE; UP TO MAX RAM SIZE)
	 * Params: File file => YAVC file to be deflated
	 */
	public String slice(File file) {
		try {
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
			ZipEntry zipEntry = zipIn.getNextEntry();
			
			this.cache = new File(file.getAbsolutePath() + ".part");
			this.cache.createNewFile();
			
			byte[] buffer = new byte[4096];
			
			while (zipEntry != null) {
				FileOutputStream fos = new FileOutputStream(this.cache);
				
				int bytesRead;
				
		        while ((bytesRead = zipIn.read(buffer)) != -1) {
		            fos.write(buffer, 0, bytesRead);
		        }
				
		        fos.close();
				zipIn.closeEntry();
				zipEntry = zipIn.getNextEntry();
			}
			
			zipIn.close();
			this.content = new String(Files.readAllBytes(Path.of(this.cache.getAbsolutePath())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return this.content;
	}
}
