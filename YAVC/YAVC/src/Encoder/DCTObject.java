package Encoder;

import java.awt.Point;

import Main.config;

public class DCTObject {
	double[][] Y = new double[config.MAKRO_BLOCK_SIZE][config.MAKRO_BLOCK_SIZE];
	double[][] CbDCT = new double[config.MAKRO_BLOCK_SIZE / 2][config.MAKRO_BLOCK_SIZE / 2];
	double[][] CrDCT = new double[config.MAKRO_BLOCK_SIZE / 2][config.MAKRO_BLOCK_SIZE / 2];
	Point Position = new Point(0, 0);
	
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
