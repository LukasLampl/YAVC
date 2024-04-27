package Utils;

import java.awt.Point;

public class DCTObject {
	private double[][] Y = null;
	private double[][] CbDCT = null;
	private double[][] CrDCT = null;
	private final int size = 4;
	private Point Position = null;
	
	public DCTObject(double[][] YDCT, double[][] CbDCT, double[][] CrDCT, Point pos) {
		this.Y = YDCT;
		this.CbDCT = CbDCT;
		this.CrDCT = CrDCT;
		this.Position = pos;
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
	public int getSize() {
		return this.size;
	}
}
