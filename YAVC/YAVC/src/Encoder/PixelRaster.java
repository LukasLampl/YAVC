package Encoder;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class PixelRaster {
	private int[] pixels = null;
	private Dimension dim = null;
	
	public PixelRaster() {}
	
	public PixelRaster(BufferedImage img) {
		invoke_pixels(img);
	}
	
	public void invoke_pixels(BufferedImage img) {
		this.dim = new Dimension(img.getWidth(), img.getHeight());

		this.pixels = new int[img.getHeight() * img.getWidth()];
		int pxPtr = 0;
		
		byte[] data = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
		
		if (img.getAlphaRaster() != null) {
			final int pixelLength = 4; //Red, Green, Blue, Alpha
			
			for (int i = 0; i < data.length; i += pixelLength) {
				int col = 0;
				col += ((int)data[i] & 0xFF) << 24; //Alpha
				col += ((int)data[i + 1] & 0xFF); //Blue
				col += ((int)data[i + 2] & 0xFF) << 8; //Green
				col += ((int)data[i + 3] & 0xFF) << 16; //Red
				
				this.pixels[pxPtr++] = col;
			}
		} else {
			final int pixelLength = 3; //Red, Green, Blue
			
			for (int i = 0; i < data.length; i += pixelLength) {
				int col = -16777216;
				col += ((int)data[i] & 0xFF); //Blue
				col += ((int)data[i + 1] & 0xFF) << 8; //Green
				col += ((int)data[i + 2] & 0xFF) << 16; //Red
				
				this.pixels[pxPtr++] = col;
			}
		}
	}
	
	public void setHeight(int height) {
		this.dim.setSize(this.dim == null ? 0 : this.dim.width, height);
	}
	
	public int getHeight() {
		return this.dim == null ? -1 : this.dim.height;
	}
	
	public void setWidth(int width) {
		this.dim.setSize(width, this.dim == null ? 0 : this.dim.height);
	}
	
	public void setDimension(Dimension dim) {
		this.dim = dim;
	}
	
	public void setPixels(int[] pixels) {
		this.pixels = pixels;
	}
	
	public int getWidth() {
		return this.dim == null ? -1 : this.dim.width;
	}
	
	public int getRGB(int x, int y) {
		if (x < 0 || x > this.dim.width) {
			System.err.println("Trying to access a pixel that is out of bounds! (X-Value)");
			return -1;
		} else if (y < 0 || y > this.dim.width) {
			System.err.println("Trying to access a pixel that is out of bounds! (Y-Value)");
			return -1;
		}
		
		int pos = (y * this.dim.width) + x;
		return this.pixels[pos];
	}
	
	public PixelRaster copy() {
		PixelRaster raster = new PixelRaster();
		raster.setDimension(this.dim);
		raster.setPixels(this.pixels);
		
		return raster;
	}
}
