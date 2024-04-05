package Encoder;

import java.awt.Point;

import Main.config;

public class YUVMakroBlock {
	private YUVColor[][] colors = new YUVColor[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
	private Point position = new Point(0, 0);
	private String ID = "";
	
	public YUVMakroBlock(YUVColor[][] colors, Point position) {
		this.colors = colors;
		this.position = position;
	}
	
	public YUVColor[][] getColors() {
		return colors;
	}
	
	public void setColor(YUVColor color, int x, int y) {
		this.colors[y][x] = color;
	}
	
	public void setColors(YUVColor[][] colors) {
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
}
