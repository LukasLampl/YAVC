package Utils;

public class YCbCrColor {
	private double Y = 0.0D;
	private double Cb = 0.0D;
	private double Cr = 0.0D;
	private int A = 0;
	
	public YCbCrColor(double Y, double Cb, double Cr) {
		this.Y = Y;
		this.Cb = Cb;
		this.Cr = Cr;
	}
	
	public YCbCrColor(double Y, double Cb, double Cr, int A) {
		this.Y = Y;
		this.Cb = Cb;
		this.Cr = Cr;
		this.A = A;
	}
	
	public YCbCrColor() {};
	
	public int getA() {
		return A;
	}
	
	public void setA(int a) {
		this.A = a;
	}

	public double getY() {
		return Y;
	}

	public void setY(double y) {
		this.Y = y;
	}

	public double getCb() {
		return Cb;
	}

	public void setCb(double Cb) {
		this.Cb = Cb;
	}

	public double getCr() {
		return Cr;
	}

	public void setCr(double Cr) {
		this.Cr = Cr;
	}
}
