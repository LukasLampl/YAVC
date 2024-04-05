package Encoder;

import java.awt.Color;

public class ColorManager {
	/*
	 * Purpose: Convert a RGB color to an YUV color
	 * Return Type: YUVColor => Converted color
	 * Params: Color color => Color to be converted
	 */
	public YUVColor convert_RGB_to_YUV(Color color) {
		double Y = 0.257 * color.getRed() + 0.504 * color.getGreen() + 0.098 * color.getBlue() + 16;
		double U = -0.148 * color.getRed() - 0.291 * color.getGreen() + 0.439 * color.getBlue() + 128;
		double V = 0.439 * color.getRed() - 0.368 * color.getGreen() - 0.071 * color.getBlue() + 128;
		return new YUVColor(Y, U, V);
	}
}