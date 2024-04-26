package Utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Filter {
	private ColorManager COLOR_MANAGER = new ColorManager();
	
	public BufferedImage apply_gaussian_blur(BufferedImage img, int radius) {
		BufferedImage render = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		double sigma = Math.max(((double)radius / 2), 1);
		double[][] kernel = new double[radius][radius];
		int mean = radius / 2;
		
		double sum = 0.0D;
		
		for (int x = 0; x < radius; x++) {
			for (int y = 0; y < radius; y++) {
				double value = Math.exp(-0.5 * (Math.pow((x - mean) / sigma, 2) + Math.pow((y - mean) / sigma, 2)) / (2 * Math.PI * sigma * sigma));
				
				kernel[x][y] = value;
				sum += value;
			}
		}
		
		for (int x = 0; x < kernel.length; x++) {
			for (int y = 0; y < kernel[x].length; y++) {
				kernel[x][y] /= sum;
			}
		}
		
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int r = 0, g = 0, b = 0;
				
				for (int kx = 0; kx < radius; kx++) {
					for (int ky = 0; ky < radius; ky++) {
						int pX = Math.min(Math.max(x + kx, 0), img.getWidth() - 1);
						int pY = Math.min(Math.max(y + ky, 0), img.getHeight() - 1);
						Color col = new Color(img.getRGB(pX, pY));
						
						double kVal = kernel[kx][ky];
						r += (int)(col.getRed() * kVal);
						g += (int)(col.getGreen() * kVal);
						b += (int)(col.getBlue() * kVal);
					}
				}
				
				render.setRGB(x, y, new Color(r, g, b).getRGB());
			}
		}
		
		return render;
	}
	
	private int[][] sobelX = {{3, 0, -3}, {10, 0, -10}, {3, 0, -3}};
	private int[][] sobelY = {{3, 10, 3}, {0, 0, 0}, {-3, -10, -3}};
	private int c = 0;
	
	public int[][] get_sobel_values(PixelRaster img) {
		int[][] values = new int[img.getWidth()][img.getHeight()];
		BufferedImage r = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		for (int x = 0; x < img.getWidth() - 2; x++) {
			for (int y = 0; y < img.getHeight() - 2; y++) {
				double gX = (sobelX[0][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y)) +
							sobelX[0][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y)) +
							sobelX[0][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y))) +
							(sobelX[1][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y + 1)) +
							sobelX[1][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y + 1)) +
							sobelX[1][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y + 1))) +
							(sobelX[2][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y + 2)) +
							sobelX[2][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y + 2)) +
							sobelX[2][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y + 2)));
				
				double gY = (sobelY[0][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y)) +
						sobelY[0][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y)) +
						sobelY[0][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y))) +
						(sobelY[1][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y + 1)) +
						sobelY[1][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y + 1)) +
						sobelY[1][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y + 1))) +
						(sobelY[2][0] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x, y + 2)) +
						sobelY[2][1] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 1, y + 2)) +
						sobelY[2][2] * this.COLOR_MANAGER.convert_RGB_to_GRAYSCALE(img.getRGB(x + 2, y + 2)));
				
				int val = (int)Math.sqrt(gX * gX + gY * gY);
				values[x][y] = val;
				r.setRGB(x, y, new Color(Math.min(val, 255), Math.min(val, 255), Math.min(val, 255)).getRGB());
			}
		}
		
		try {
			ImageIO.write(r, "png", new File("C:\\Users\\Lukas Lampl\\Documents\\Res\\G_" + c++ + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return values;
	}
	
	/*
	 * Purpose: Damp the RGB color of ´´´img2´´´ so a lot of redundancy appears
	 * Return Type: void => img2 is set automatically
	 * Params: BufferedImage img1 => Reference image;
	 * 			BufferedImage img2 => Image to damp;
	 */
	public void damp_frame_colors(PixelRaster img1, PixelRaster img2) {
		//Error on dimension mismatching
		if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
			System.err.println("Images are not compareable!");
			return;
		}
		
		int width = img1.getWidth();
		int height = img1.getHeight();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel1 = img1.getRGB(x, y);
				int pixel2 = img2.getRGB(x, y);
				YCbCrColor prevCol = this.COLOR_MANAGER.convert_RGB_to_YCbCr(pixel1);
				YCbCrColor curCol = this.COLOR_MANAGER.convert_RGB_to_YCbCr(pixel2);
				
				double deltaY = Math.abs(prevCol.getY() - curCol.getY());
				double deltaCb = Math.abs(prevCol.getCb() - curCol.getCb());
				double deltaCr = Math.abs(prevCol.getCr() - curCol.getCr());
				
				if (deltaY > 2 || deltaCb > 6 || deltaCr > 6) {
					continue;
				}
				
				img2.setRGB(x, y, pixel1);
			}
		}
	}
}
