package Utils;

import java.awt.Point;

public class DCTObject {
	private double[][] Y = null;
	private double[][] CbDCT = null;
	private double[][] CrDCT = null;
	private int size = 0;
	private Point Position = new Point(0, 0);
	
	public DCTObject(double[][] YDCT, double[][] CbDCT, double[][] CrDCT, Point pos, int size) {
		this.Y = YDCT;
		this.CbDCT = CbDCT;
		this.CrDCT = CrDCT;
		this.Position = pos;
		this.size = size;
	}
	
	public double[][] getY() {
		return Y;
	}
	public void setY(double[][] y) {
		Y = y;
	}
	public double[][] getCbDCT() {
		return CbDCT;
	}
	public void setCbDCT(double[][] cbDCT) {
		CbDCT = cbDCT;
	}
	public double[][] getCrDCT() {
		return CrDCT;
	}
	public void setCrDCT(double[][] crDCT) {
		CrDCT = crDCT;
	}
	public void setPosition(Point position) {
		Position = position;
	}
	public Point getPosition() {
		return Position;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getSize() {
		return this.size;
	}
}
