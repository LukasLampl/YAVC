package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

public class ToggleButton extends JButton {
	private static final long serialVersionUID = 1L;
	private boolean isClicked = true;
	
	public ToggleButton(String text) {
		super(text);
	}
	
	public boolean isClicked() {
		return isClicked;
	}
	public void setClicked(boolean isClicked) {
		this.isClicked = isClicked;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D)g.create();
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setColor(getBackground());
		g2D.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
		g2D.setColor(Color.WHITE);
		g2D.drawString(getText(), 17, 20);
		g2D.dispose();
	}
}
