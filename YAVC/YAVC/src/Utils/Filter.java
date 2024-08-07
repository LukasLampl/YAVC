/////////////////////////////////////////////////////////////
///////////////////////    LICENSE    ///////////////////////
/////////////////////////////////////////////////////////////
/*
The YAVC video / frame compressor compresses frames.
Copyright (C) 2024  Lukas Nian En Lampl

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package Utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;

public class Filter {
	private ColorManager COLOR_MANAGER = new ColorManager();
	
	/*
	 * Purpose: Apply the gaussian blur filter to an image
	 * Return Type: BufferedImage => Blurred image
	 * Params: BufferedImage img => Image to blur;
	 * 			int radius => Strength of the blurring
	 */
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

	/*
	 * Purpose: Get the Sobel-Scharr values from the image for Edge & Texture detection
	 * Return Type: void
	 * Params: PixelRaster img => Image to get the Sobel magnitude from;
	 * 			int[][] array => Array to store the values inside;
	 * 			int[][] colorHistogram => Histogram of the colors in the current frame
	 */
	private int[][] sobelX = {{3, 0, -3}, {10, 0, -10}, {3, 0, -3}};
	private int[][] sobelY = {{3, 10, 3}, {0, 0, 0}, {-3, -10, -3}};
	private BufferedImage sobel_image = null;
	private HashSet<Color> currentColors = new HashSet<Color>();
	
	public void get_sobel_values(PixelRaster img, int[][] array, int[][] colorHistogram) {
		if (img == null) {
			System.err.println("No image to process! > Skip");
			return;
		} else if (array == null) {
			System.err.println("Array for sobel values NULL, can't store in NULL!");
			return;
		} else if (colorHistogram == null) {
			System.err.println("Colorhistogram not initialized or NULL!");
			return;
		}

		boolean firstColInHistogram = true;
		this.currentColors.clear();
		this.sobel_image = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		for (int x = 0; x < img.getWidth() - 2; x++) {
			for (int y = 0; y < img.getHeight() - 2; y++) {
				Color col = new Color(img.getRGB(x, y));
				
				if (firstColInHistogram) {
					colorHistogram[0][col.getRed()] = 1;
					colorHistogram[1][col.getGreen()] = 1;
					colorHistogram[2][col.getBlue()] = 1;
					firstColInHistogram = false;
				} else {
					colorHistogram[0][col.getRed()]++;
					colorHistogram[1][col.getGreen()]++;
					colorHistogram[2][col.getBlue()]++;
				}
				this.currentColors.add(col);
				
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
				
				int val = Math.min((int)Math.sqrt(gX * gX + gY * gY), 255);
				array[x][y] = val;
			}
		}
		
		supress_noise(array, 0.65, 255);
	}

	private void supress_noise(int[][] sobel, double tolerance, int max) {
		int tol = (int)(max - max * tolerance);
		
		for (int x = 0; x < sobel.length; x++) {
			for (int y = 0; y < sobel[x].length; y++) {
				if (sobel[x][y] < tol) {
					sobel[x][y] = 0;
				} else if (sobel[x][y] > tol && sobel[x][y] < (tol + max * tolerance / 2)) {
					sobel[x][y] = 128;
				} else {
					sobel[x][y] = 255;
				}
				
				sobel_image.setRGB(x, y, new Color(sobel[x][y], sobel[x][y], sobel[x][y]).getRGB());
			}
		}
	}
	
	public BufferedImage get_sobel_image() {
		return this.sobel_image == null ? new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB) : this.sobel_image;
	}
	
	public int get_color_count() {
		return this.currentColors.size();
	}
	
	/*
	 * Purpose: Damp the RGB color of ´´´img2´´´ so a lot of redundancy appears
	 * Return Type: void => img2 is set automatically
	 * Params: PixelRaster img1 => Reference image;
	 * 			PixelRaster img2 => Image to damp;
	 */
	public void damp_frame_colors(PixelRaster img1, PixelRaster img2) {
		if (img1 == null || img2 == null) {
			System.err.println("No frames to damp colors in!");
			return;
		}
		
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
				double[] prevCol = this.COLOR_MANAGER.convert_RGB_to_YCbCr(pixel1);
				double[] curCol = this.COLOR_MANAGER.convert_RGB_to_YCbCr(pixel2);
				
				double deltaY = Math.abs(prevCol[0] - curCol[0]);
				double deltaCb = Math.abs(prevCol[1] - curCol[1]);
				double deltaCr = Math.abs(prevCol[2] - curCol[2]);
				
				if (deltaY > 3.0 || deltaCb > 8.0 || deltaCr > 8.0) {
					continue;
				}
				
				img2.setRGB(x, y, pixel1);
			}
		}
	}
	
	public void flatten_down_color(ArrayList<YCbCrMakroBlock> blocks) {
		for (YCbCrMakroBlock b : blocks) {
			//Flatten Y-Component
			for (int x = 0; x < b.getSize(); x++) {
				for (int y = 0; y < b.getSize(); y++) {
					double Y0 = b.getYVal(x, y), Y1, Y2;
					
					if (x + 1 < b.getSize()) {
						Y1 = b.getYVal(x + 1, y);
					} else {
						Y1 = b.getYVal(x - 1, y);
					}
					
					if (x + 2 < b.getSize()) {
						Y2 = b.getYVal(x + 2, y);
					} else {
						Y2 = b.getYVal(x - 2, y);
					}
					
					if (Math.abs(Y0 - Y1) > 2.5) continue;
					if (Math.abs(Y1 - Y2) < 3.5 && Math.abs(Y1 - Y2) > 2.0) continue;
					
					b.setYVal(x, y, (Y0 + Y1) / 2);
				}
			}
			
			//Flatten Cb & Cr Component
			for (int x = 0; x < b.getSize() / 2; x++) {
				for (int y = 0; y < b.getSize() / 2; y++) {
					double Cb0 = b.getChromaCb(x, y), Cb1, Cb2;
					double Cr0 = b.getChromaCb(x, y), Cr1, Cr2;
					
					if (x + 1 < b.getSize() / 2) {
						Cb1 = b.getChromaCb(x + 1, y);
						Cr1 = b.getChromaCr(x + 1, y);
					} else if (x - 1 >= 0) {
						Cb1 = b.getChromaCb(x - 1, y);
						Cr1 = b.getChromaCr(x - 1, y);
					} else continue;
					
					if (x + 2 < b.getSize() / 2) {
						Cb2 = b.getChromaCb(x + 2, y);
						Cr2 = b.getChromaCr(x + 2, y);
					} else if (x - 2 >= 0) {
						Cb2 = b.getChromaCb(x - 2, y);
						Cr2 = b.getChromaCr(x - 1, y);
					} else continue;
					
					if (Math.abs(Cb0 - Cb1) > 4.0 || Math.abs(Cr0 - Cr1) > 4.0) continue;
					if ((Math.abs(Cb1 - Cb2) < 4.5 && Math.abs(Cb1 - Cb2) > 3.0)
						|| (Math.abs(Cr1 - Cr2) < 4.5 && Math.abs(Cr1 - Cr2) > 3.0)) continue;
					
					b.setChroma(x, y, (Cb0 + Cb1) / 2, (Cr0 + Cr1) / 2);
				}
			}
		}
	}
	
	public void apply_deblocking(PixelRaster result, ArrayList<YCbCrMakroBlock> blocks) {
		
	}
}
