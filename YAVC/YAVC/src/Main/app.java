package Main;

import javax.swing.UIManager;

import UI.Frame;

public class app {
	public static void main(String [] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }

		Frame f = new Frame();
		f.setSize(700, 600);
		f.setVisible(true);
	}
}
