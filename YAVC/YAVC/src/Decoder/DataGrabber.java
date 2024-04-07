package Decoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataGrabber {
	private File cache = null;
	
	/*
	 * Purpose: Resolve the deflate the YAVC file
	 * Return Type: String => Content of the YAVC file (CAN GET REALLY REALLY HUGE; UP TO MAX RAM SIZE)
	 * Params: File file => YAVC file to be deflated
	 */
	public void slice(File file) {
		try {
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
			ZipEntry zipEntry = zipIn.getNextEntry();
			
			this.cache = new File(file.getAbsolutePath() + ".part");
			this.cache.mkdir();
			
			byte[] buffer = new byte[4096];
			
			while (zipEntry != null) {
				FileOutputStream fos = new FileOutputStream(this.cache + "/" + zipEntry.getName());
				
				int bytesRead;
				
		        while ((bytesRead = zipIn.read(buffer)) != -1) {
		            fos.write(buffer, 0, bytesRead);
		        }
				
		        fos.close();
				zipIn.closeEntry();
				zipEntry = zipIn.getNextEntry();
			}
			
			zipIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String get_metadata() {
		String content = null;
		File md_file = new File(this.cache.getAbsolutePath() + "/META.DESC");
		
		if (!md_file.exists()) {
			System.err.println("No meta data found, file corrupted?");
			System.exit(0);
		}
		
		try {
			content = new String(Files.readAllBytes(Path.of(md_file.getAbsolutePath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return content;
	}
	
	public String get_start_frame() {
		String content = null;
		File sf_file = new File(this.cache.getAbsolutePath() + "/SF.YAVCF");
		
		if (!sf_file.exists()) {
			System.err.println("No start frame found!");
			System.exit(0);
		}
		
		try {
			content = new String(Files.readAllBytes(Path.of(sf_file.getAbsolutePath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return content;
	}
	
	public String get_frame(int frameNumber) {
		String content = null;
		File f_file = new File(this.cache.getAbsolutePath() + "/F_" + frameNumber + ".YAVCF");
		
		if (!f_file.exists()) {
			System.err.println("No frame with the number " + frameNumber + " found!");
			return null;
		}
		
		try {
			content = new String(Files.readAllBytes(Path.of(f_file.getAbsolutePath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return content;
	}
}
