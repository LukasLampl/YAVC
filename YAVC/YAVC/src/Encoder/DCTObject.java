package Encoder;

import java.awt.Point;

import Main.config;

public class DCTObject {
	private double[][] Y = new double[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
	private double[][] CbDCT = new double[config.MAKRO_BLOCK_SIZE / 2][config.MAKRO_BLOCK_SIZE / 2];
	private double[][] CrDCT = new double[config.MAKRO_BLOCK_SIZE / 2][config.MAKRO_BLOCK_SIZE / 2];
	private Point Position = new Point(0, 0);
	
	public DCTObject() {}
	
	public DCTObject(double[][] YDCT, double[][] CbDCT, double[][] CrDCT, Point pos) {
		this.Y = YDCT;
		this.CbDCT = CbDCT;
		this.CrDCT = CrDCT;
		this.Position = pos;
	}
	
	public DCTObject(double[][] YDCT, double[][] CbDCT, double[][] CrDCT) {
		this.Y = YDCT;
		this.CbDCT = CbDCT;
		this.CrDCT = CrDCT;
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
}
