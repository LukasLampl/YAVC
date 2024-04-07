package Encoder;

import java.awt.Point;

import Main.config;

public class YCbCrMakroBlock {
	private YCbCrColor[][] colors = new YCbCrColor[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
	private Point position = new Point(0, 0);
	private String ID = "";
	private boolean edgeBlock = false;
	
	public YCbCrMakroBlock(YCbCrColor[][] colors, Point position) {
		this.colors = colors;
		this.position = position;
	}
	
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
	
	public void setID(String id) {
		this.ID = id;
	}
	
	public String getID() {
		return this.ID;
	}

	public boolean isEdgeBlock() {
		return edgeBlock;
	}

	public void setEdgeBlock(boolean edgeBlock) {
		this.edgeBlock = edgeBlock;
	}
}
