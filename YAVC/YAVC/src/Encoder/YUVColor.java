package Encoder;

public class YUVColor {
	private double Y = 0.0D;
	private double U = 0.0D;
	private double V = 0.0D;
	
	public YUVColor(double Y, double U, double V) {
		this.Y = Y;
		this.U = U;
		this.V = V;
	}

	public double getY() {
		return Y;
	}

	public void setY(double y) {
		Y = y;
	}

	public double getU() {
		return U;
	}

	public void setU(double u) {
		U = u;
	}

	public double getV() {
		return V;
	}

	public void setV(double v) {
		V = v;
	}
	
	
}
