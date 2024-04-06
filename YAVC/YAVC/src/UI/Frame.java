package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Encoder.MakroBlock;
import Main.config;

public class Frame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private double DAMING_TOLERANCE = 0.75;
	private int EDGE_TOLERANCE = 4;
	private int GRAYSCALE_TOLERANCE = 100;
	private double MAGNITUDE_TOLERANCE = 0.0125;
	
	private JLabel prevFrameHolder = new JLabel();
	private JLabel curFrameHolder = new JLabel();
	private JLabel diffsHolder = new JLabel();
	private JLabel vectorDiffsHolder = new JLabel();
	private JLabel frameProgress = new JLabel("NA/NA");
	private JProgressBar progressBar = new JProgressBar();
	
	private JPanel interactivePanel = new JPanel();
	
	public Frame(Dimension dim) {
		this.setTitle("YAVC");
		this.setSize(dim);
		this.setPreferredSize(dim);
		this.setMaximumSize(dim);
		this.setLayout(new BorderLayout());
		
		JPanel propsPanel = set_up_props_panel();
		this.add(propsPanel, BorderLayout.NORTH);
		
		JPanel prevPanel = set_up_preview_panel();
		this.add(prevPanel, BorderLayout.CENTER);
		
		JPanel interPanel = set_up_interactive_panel();
		this.add(interPanel, BorderLayout.SOUTH);
	}
	
	/*
	 * Purpose: Setup the preview for the compression progress
	 * Return Type: JPanel => JPanel with the preview parts
	 * Params: void
	 */
	private JPanel set_up_preview_panel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		this.prevFrameHolder.setBorder(BorderFactory.createTitledBorder("Previous Frame"));
		this.curFrameHolder.setBorder(BorderFactory.createTitledBorder("Current Frame"));
		this.diffsHolder.setBorder(BorderFactory.createTitledBorder("Differences to previous Frame"));
		this.vectorDiffsHolder.setBorder(BorderFactory.createTitledBorder("Vectorized differences"));
		this.progressBar.setStringPainted(true);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy++;
		cons.gridwidth = 1;
		panel.add(this.prevFrameHolder, cons);
		cons.gridx = 1;
		panel.add(this.curFrameHolder, cons);
		cons.gridx = 0;
		cons.gridy++;
		panel.add(this.diffsHolder, cons);
		cons.gridx = 1;
		panel.add(this.vectorDiffsHolder, cons);
		cons.gridx = 0;
		cons.gridy++;
		cons.gridwidth = 2;
		panel.add(this.frameProgress, cons);
		cons.gridy++;
		panel.add(progressBar, cons);
		
		return panel;
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
				DAMING_TOLERANCE = ((double)dampingSlider.getValue() / (double)100);
			}
		});
		
		panel.add(dampingSlider, cons);
		
		cons.gridy++;
		
		JSlider grayscaleSlider = new JSlider();
		grayscaleSlider.setValue(this.GRAYSCALE_TOLERANCE);
		grayscaleSlider.setMinimum(0);
		grayscaleSlider.setMaximum(1000);
		grayscaleSlider.setPaintLabels(true);
		grayscaleSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				GRAYSCALE_TOLERANCE = grayscaleSlider.getValue();
			}
		});
		
		panel.add(grayscaleSlider, cons);
		
		cons.gridy++;
		
		JSlider magnitudeSlider = new JSlider();
		magnitudeSlider.setValue((int)(this.MAGNITUDE_TOLERANCE * 1000));
		magnitudeSlider.setMinimum(0);
		magnitudeSlider.setMaximum(100);
		magnitudeSlider.setPaintLabels(true);
		magnitudeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				MAGNITUDE_TOLERANCE = (double)magnitudeSlider.getValue() / 1000;
			}
		});
		
		panel.add(magnitudeSlider, cons);
		
		return panel;
	}
	
	/*
	 * Purpose: Setup the panel, that will provide the user controls like "Decode" and "Encode"
	 * Return Type: JPanel => Setup JPanel
	 * Params: void
	 */
	private JPanel set_up_interactive_panel() {
		this.interactivePanel.setLayout(new GridBagLayout());
		return this.interactivePanel;
	}
	
	/*
	 * Purpose: Adds the encode button to the interactive jpanel 
	 * Return Type: void
	 * Params: JButton btn => Encode button
	 */
	public void setEncodeBtn(JButton btn) {
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 0;
		cons.gridwidth = 1;
		this.interactivePanel.add(btn, cons);
		update();
	}
	
	/*
	 * Purpose: Adds the decode button to the interactive jpanel 
	 * Return Type: void
	 * Params: JButton btn => Decode button
	 */
	public void setDecodeBtn(JButton btn) {
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = 1;
		cons.gridy = 0;
		cons.gridwidth = 1;
		this.interactivePanel.add(btn, cons);
		update();
	}
	
	public double get_magnitude_tolerance() {
		return this.MAGNITUDE_TOLERANCE;
	}
	
	public double get_damping_tolerance() {
		return this.DAMING_TOLERANCE;
	}
	
	public int get_edge_tolerance() {
		return this.EDGE_TOLERANCE;
	}
	
	public int get_grayscale_tolerance() {
		return this.GRAYSCALE_TOLERANCE;
	}
	
	public void updateFrameCount(int currentFrame, int totalFrame) {
		this.frameProgress.setText(currentFrame + "/" + totalFrame + "Frames");
		update();
	}
	
	public void setProgress(int progress) {
		this.progressBar.setValue(progress);
	}
	
	/*
	 * Purpose: Sets the difference image in the encoding process for preview
	 * Return Type: void
	 * Params: ArrayList<MakroBlock> differences => Found differences between two frames (Without vectors);
	 * 			Dimension dim => Dimension of the difference image
	 */
	public void setDifferenceImage(ArrayList<MakroBlock> differences, Dimension dim) {
		BufferedImage render = render_image(differences, dim);
		int width = this.prevFrameHolder.getIcon().getIconWidth();
		int height = this.prevFrameHolder.getIcon().getIconHeight();
		
		this.diffsHolder.setIcon(new ImageIcon(render.getScaledInstance(width, height, Image.SCALE_FAST)));
		update();
	}
	
	/*
	 * Purpose: Sets the vectorized image in the encoding process for preview
	 * Return Type: void
	 * Params: ArrayList<MakroBlock> differences => Found differences between two frames (With vectors);
	 * 			Dimension dim => Dimension of the voctorized image
	 */
	public void setVectorizedImage(ArrayList<MakroBlock> differences, Dimension dim) {
		BufferedImage render = render_image(differences, dim);
		int width = this.prevFrameHolder.getIcon().getIconWidth();
		int height = this.prevFrameHolder.getIcon().getIconHeight();
		
		this.vectorDiffsHolder.setIcon(new ImageIcon(render.getScaledInstance(width, height, Image.SCALE_FAST)));
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
		float factor = (float)img1.getHeight() / (float)img1.getWidth();
		float width = ((float)this.getWidth() / 2) - (float)this.getWidth() / 16 * 2;
		float height = factor * width;
		
		this.prevFrameHolder.setIcon(new ImageIcon(img1.getScaledInstance((int)width, (int)height, Image.SCALE_FAST)));
		this.curFrameHolder.setIcon(new ImageIcon(img2.getScaledInstance((int)width, (int)height, Image.SCALE_FAST)));
		update();
	}
	
	private void update() {
		this.repaint();
		this.revalidate();
	}
}