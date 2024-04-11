package UI;

import javax.swing.JSlider;

public class CustomSlider extends JSlider {
	private static final long serialVersionUID = 1L;

	public CustomSlider(int min, int max, int init) {
		setMinimum(min);
		setMaximum(max);
		setValue(init);
		setUI(new CustomSliderUI(this));
	}
}
