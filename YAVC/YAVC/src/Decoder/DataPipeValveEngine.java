package Decoder;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class DataPipeValveEngine {
	private File output = null;
	
	public DataPipeValveEngine(String path) {
		this.output = new File(path + "/FRAMES");
		this.output.mkdir();
	}
	
	private int frameCounter = 0;
	
	public void release_image(BufferedImage img) {
		try {
			ImageIO.write(img, "png", new File(output.getAbsolutePath() + "/" + (this.frameCounter++) + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
