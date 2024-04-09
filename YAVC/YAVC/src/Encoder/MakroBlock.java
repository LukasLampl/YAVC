package Encoder;

import java.awt.Point;

import Main.config;

public class MakroBlock {
	private int[][] colors = new int[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
	private Point position = new Point(0, 0);
	private boolean isEdgeBlock = false;
	
	public MakroBlock(int[][] colors, Point position) {
		this.colors = colors;
		this.position = position;
	}
	
	public int[][] getColors() {
		return colors;
	}
	
	public void setColors(int[][] colors) {
		this.colors = colors;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public void setPosition(Point position) {
		this.position = position;
	}

	public boolean isEdgeBlock() {
		return isEdgeBlock;
	}

	public void setEdgeBlock(boolean isEdgeBlock) {
		this.isEdgeBlock = isEdgeBlock;
	}
}
