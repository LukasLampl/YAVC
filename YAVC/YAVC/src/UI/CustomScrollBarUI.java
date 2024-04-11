package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class CustomScrollBarUI extends BasicScrollBarUI {
	private final Dimension d = new Dimension();
   
	@Override
	protected JButton createDecreaseButton(int orientation) {
    	return new JButton() {
			private static final long serialVersionUID = 1L;

			@Override
	    	public Dimension getPreferredSize() {
	    		return d;
	        }
    	};
    }
    
    @Override
    protected JButton createIncreaseButton(int orientation) {
      return new JButton() {
		private static final long serialVersionUID = 1L;

		@Override
        public Dimension getPreferredSize() {
          return d;
        }
      };
    }
    
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    	Graphics2D g2D = (Graphics2D)g.create();
    	g2D.setColor(ComponentColor.DEFAULT_COLOR);
    	g2D.fillRect(r.x, r.y, r.width, r.height);
    	g2D.dispose();
    }
    
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    	Color color = null;
    	JScrollBar sb = (JScrollBar)c;
    	Graphics2D g2D = (Graphics2D)g.create();
    	g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	
    	if(!sb.isEnabled() || r.width>r.height) {
    		return;
    	} else if (isDragging) {
    		color = ComponentColor.HIGHLIGHT_SHADE_DIS_COLOR;
    	} else if (isThumbRollover()) {
    		color = ComponentColor.HIGHLIGHT_SHADE_DIS_LIGHT_COLOR;
    	} else {
    		color = ComponentColor.HIGHLIGHT_SHADE_DIS_COLOR;
    	}
    	
    	g2D.setPaint(color);
    	g2D.fillRoundRect(r.x + 5, r.y, 5, r.height, 5, 5);
    	g2D.dispose();
    }
    
    @Override
    protected void setThumbBounds(int x, int y, int width, int height) {
    	super.setThumbBounds(x, y, width, height);
      	scrollbar.repaint();
    }
}
