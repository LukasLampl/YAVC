package Encoder;

import java.awt.Point;

import Main.config;

public class YCbCrMakroBlock {
	private YCbCrColor[][] colors = new YCbCrColor[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
	private Point position = new Point(0, 0);
	private double SAD = Float.MAX_VALUE;
	private int referenceDrawback = 0;
	private boolean edgeBlock = false;
	
	public YCbCrMakroBlock(YCbCrColor[][] colors, Point position) {
		this.colors = colors;
		this.position = position;
	}
	
	public YCbCrMakroBlock() {}
	
	public YCbCrColor[][] getColors() {
		return colors;
	}
	
	public void setColor(YCbCrColor color, int x, int y) {
		this.colors[y][x] = color;
	}
	
	public void setColors(YCbCrColor[][] colors) {
		this.colors = colors;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public void setPosition(Point position) {
		this.position = position;
	}

	public boolean isEdgeBlock() {
		return edgeBlock;
	}

	public void setEdgeBlock(boolean edgeBlock) {
		this.edgeBlock = edgeBlock;
	}

	public double getSAD() {
		return SAD;
	}

	public void setSAD(double sAD) {
		SAD = sAD;
	}

	public int getReferenceDrawback() {
		return referenceDrawback;
	}

	public void setReferenceDrawback(int referenceDrawback) {
		this.referenceDrawback = referenceDrawback;
	}
}
