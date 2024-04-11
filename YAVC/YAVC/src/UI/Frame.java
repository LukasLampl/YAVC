package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Encoder.MakroBlock;
import Main.config;

public class Frame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private float DAMING_TOLERANCE = 0.75F;
	private int EDGE_TOLERANCE = 4;
	private int VEC_SAD_TOLERANCE = 32768;
	
	private boolean WRITER_ACTIVE = true;
	
	private EncodePanel ENCODE_PANEL = new EncodePanel(this);
	private DecodePanel DECODE_PANEL = new DecodePanel();
	private Sidebar SIDEBAR = new Sidebar(this.ENCODE_PANEL, this.DECODE_PANEL, this);
	private JPanel CURRENT_FOCUS = null;
	
	public Frame() {
		this.setTitle("YAVC");
		this.setLayout(new BorderLayout());
	
		WelcomePanel welcome = new WelcomePanel();
		
		this.add(this.SIDEBAR, BorderLayout.WEST);
		this.add(welcome, BorderLayout.CENTER);
		this.CURRENT_FOCUS = welcome;
	}
	
	public void move_focused_panel(JPanel newFocus) {
		if (this.CURRENT_FOCUS != null) {
			this.remove(this.CURRENT_FOCUS);
		}
		
		this.CURRENT_FOCUS = newFocus;
		this.add(newFocus, BorderLayout.CENTER);
		update();
	}
	
	/*
	 * Purpose: Create the panel, that contains the adjustment sliders
	 * Return Type: JPanel => JPanel with sliders
	 * Params: void
	 */
	private JPanel set_up_props_panel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 0;
		
		JSlider edgeSlider = new JSlider();
		edgeSlider.setValue(this.EDGE_TOLERANCE);
		edgeSlider.setMinimum(0);
		edgeSlider.setMaximum(100);
		edgeSlider.setPaintLabels(true);
		edgeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				EDGE_TOLERANCE = edgeSlider.getValue();
			}
		});
		
		panel.add(edgeSlider, cons);
		cons.gridy++;
		
		JSlider dampingSlider = new JSlider();
		dampingSlider.setValue((int)(this.DAMING_TOLERANCE * 100));
		dampingSlider.setMinimum(0);
		dampingSlider.setMaximum(100);
		dampingSlider.setPaintLabels(true);
		dampingSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				DAMING_TOLERANCE = ((float)dampingSlider.getValue() / (float)100);
			}
		});
		
		panel.add(dampingSlider, cons);
		cons.gridy++;
		
		JSlider vecSADSlider = new JSlider();
		vecSADSlider.setMinimum(0);
		vecSADSlider.setMaximum(255 * config.MBS_SQ * 2 + (int)Math.pow((255 * config.MBS_SQ), 2));
		vecSADSlider.setValue(this.VEC_SAD_TOLERANCE);
		vecSADSlider.setPaintLabels(true);
		vecSADSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println(vecSADSlider.getValue());
				VEC_SAD_TOLERANCE = vecSADSlider.getValue();
			}
		});
		
		panel.add(vecSADSlider, cons);
		
		return panel;
	}
	
	public boolean canWriterWrite() {
		return this.WRITER_ACTIVE;
	}
	
	public void disposeWriterPermission() {
		this.WRITER_ACTIVE = false;
	}

	public float get_damping_tolerance() {
		return this.DAMING_TOLERANCE;
	}
	
	public int get_edge_tolerance() {
		return this.EDGE_TOLERANCE;
	}
	
	public int get_vec_sad_tolerance() {
		return this.VEC_SAD_TOLERANCE;
	}
	
	public void updateFrameCount(int currentFrame, int totalFrame, boolean percentOnly) {
		this.ENCODE_PANEL.set_frame_stats(currentFrame, totalFrame, percentOnly);
		update();
	}
	
	/*
	 * Purpose: Sets the difference image in the encoding process for preview
	 * Return Type: void
	 * Params: ArrayList<MakroBlock> differences => Found differences between two frames (Without vectors);
	 * 			Dimension dim => Dimension of the difference image
	 */
	public void setDifferenceImage(ArrayList<MakroBlock> differences, Dimension dim) {
		BufferedImage render = render_image(differences, dim);
		this.ENCODE_PANEL.set_res_frame(render);
		update();
	}
	
	/*
	 * Purpose: Sets the vectorized image in the encoding process for preview
	 * Return Type: void
	 * Params: BufferedImage img => Image with the vector paths
	 */
	public void setVectorizedImage(BufferedImage img) {
		this.ENCODE_PANEL.set_vec_frame(img);
		update();
	}
	
	/*
	 * Purpose: Renders the resulting image with differences and vectors
	 * Return Type: BufferedImage => Rendered Image
	 * Params: ArrayList<MakroBlock> differences => Differences between two frames;
	 * 			Dimension dim => Dimension of the image
	 */
	private BufferedImage render_image(ArrayList<MakroBlock> differences, Dimension dim) {
		BufferedImage render = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		
		for (MakroBlock block : differences) {
			for (int y = 0; y < config.MAKRO_BLOCK_SIZE; y++) {
				for (int x = 0; x < config.MAKRO_BLOCK_SIZE; x++) {
					if (block.getPosition().x + x >= render.getWidth()
						|| block.getPosition().y + y >= render.getHeight()) {
						continue;
					}
					
					if (block.getColors()[y][x] == 89658667) { //ASCII for YAVC
						continue;
					}
					
					Color col = new Color(block.getColors()[y][x]);
					render.setRGB(block.getPosition().x + x, block.getPosition().y + y, col.getRGB());
				}
			}
		}
		
		return render;
	}
	
	/*
	 * Purpose: Resize the preview image, if the UI gets scaled
	 * Return Type: void
	 * Params: BufferedImage img1 => previous image;
	 * 			BufferedImage img2 => current image
	 */
	public void setPreviews(BufferedImage img1, BufferedImage img2) {
		this.ENCODE_PANEL.set_prev_frame(img1);
		this.ENCODE_PANEL.set_cur_frame(img2);
		update();
	}
	
	private void update() {
		this.repaint();
		this.revalidate();
	}
}
