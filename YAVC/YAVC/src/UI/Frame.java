package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Encoder.EntryPoint;
import Encoder.MakroBlock;
import Main.config;

public class Frame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private float DAMING_TOLERANCE = 0.75F;
	private int EDGE_TOLERANCE = 4;
	private int VEC_SAD_TOLERANCE = 32768;
	
	private boolean WRITER_ACTIVE = true;
	
	private EntryPoint entryPoint = new EntryPoint();
	private EncodePanel ENCODE_PANEL = new EncodePanel(this, entryPoint);
	private DecodePanel DECODE_PANEL = new DecodePanel(this, entryPoint);
	private Sidebar SIDEBAR = new Sidebar(this.ENCODE_PANEL, this.DECODE_PANEL, this);
	private JPanel CURRENT_FOCUS = null;
	
	public Frame() {
		this.setTitle("YAVC");
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	
		WelcomePanel welcome = new WelcomePanel();
		
		this.add(this.SIDEBAR, BorderLayout.WEST);
		this.add(welcome, BorderLayout.CENTER);
		this.CURRENT_FOCUS = welcome;
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				entryPoint.stop_encoding_process();
				WRITER_ACTIVE = false;
				System.exit(0);
			}
		});
	}
	
	/*
	 * Purpose: Set the desired panel into focus of the frame
	 * Return Type: void
	 * Params: JPanel newFocus => Panel that should be the new focus
	 */
	public void move_focused_panel(JPanel newFocus) {
		if (this.CURRENT_FOCUS != null) {
			this.remove(this.CURRENT_FOCUS);
		}
		
		this.CURRENT_FOCUS = newFocus;
		this.add(newFocus, BorderLayout.CENTER);
		update();
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
