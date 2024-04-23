package Utils;

import java.awt.Point;

public class YCbCrMakroBlock {
	private YCbCrColor[][] colors = null;
	private Point position = new Point(0, 0);
	private double SAD = Float.MAX_VALUE;
	private int referenceDrawback = 0;
	private int size = 0;
	private boolean edgeBlock = false;
	
	public YCbCrMakroBlock(YCbCrColor[][] colors, Point position, int size) {
		this.colors = colors;
		this.position = position;
		this.size = size;
	}
	
	public YCbCrColor[][] getColors() {
		return colors;
	}
	
	public void setColor(YCbCrColor color, int x, int y) {
		if (this.colors == null) {
			System.err.println("No colors in object!");
			return;
		}
		
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
	
	public int getSize() {
		return this.size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
}
