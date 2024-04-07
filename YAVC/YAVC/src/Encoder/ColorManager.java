package Encoder;

import java.awt.Color;

public class ColorManager {
	/*
	 * Purpose: Convert a RGB color to an YUV color
	 * Return Type: YUVColor => Converted color
	 * Params: Color color => Color to be converted
	 */
	public YCbCrColor convert_RGB_to_YCbCr(Color color) {
		double Y = 16 + (65.738 * color.getRed() / 256) + (129.057 * color.getGreen() / 256) + (25.064 * color.getBlue() / 256);
		double Cb = 128 - (37.945 * color.getRed() / 256) - (74.494 * color.getGreen() / 256) + (112.439 * color.getBlue() / 256);
		double Cr = 128 + (112.439 * color.getRed() / 256) - (94.154 * color.getGreen() / 256) - (18.285 * color.getBlue() / 256);
		
		return new YCbCrColor(Y, Cb, Cr);
	}
	
	public Color convert_YCbCr_to_RGB(YCbCrColor color) {
		int red = (int)(color.getY() + 1.402 * (color.getCr() - 128));
		int green = (int)(color.getY() - 0.344 * (color.getCb() - 128) - 0.714 * (color.getCr() - 128));
		int blue = (int)(color.getY() + 1.772 * (color.getCb() - 128));
		return new Color(red, green, blue);
	}
}