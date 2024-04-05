package Encoder;

import java.awt.Point;

public class Vector {
	private Point startingPoint = new Point(0, 0);
	private int spanX = 0;
	private int spanY = 0;
	private MakroBlock appendedBlock = null;
	private MakroBlock mostEqualBlock = null;
	
	public Point getStartingPoint() {
		return startingPoint;
	}
	
	public void setStartingPoint(Point startingPoint) {
		this.startingPoint = startingPoint;
	}
	
	public int getSpanX() {
		return this.spanX;
	}
	
	public void setSpanX(int span) {
		this.spanX = span;
	}
	
	public int getSpanY() {
		return this.spanY;
	}
	
	public void setSpanY(int span) {
		this.spanY = span;
	}

	public MakroBlock getAppendedBlock() {
		return appendedBlock;
	}

	public void setAppendedBlock(MakroBlock appendedBlock) {
		this.appendedBlock = appendedBlock;
	}

	public MakroBlock getMostEqualBlock() {
		return mostEqualBlock;
	}

	public void setMostEqualBlock(MakroBlock mostEqualBlock) {
		this.mostEqualBlock = mostEqualBlock;
	}
}
