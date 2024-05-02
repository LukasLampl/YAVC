/////////////////////////////////////////////////////////////
///////////////////////    LICENSE    ///////////////////////
/////////////////////////////////////////////////////////////
/*
The YAVC video / frame compressor compresses frames.
Copyright (C) 2024  Lukas Nian En Lampl

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Main.EntryPoint;
import Utils.ColorManager;
import Utils.PixelRaster;
import Utils.YCbCrColor;
import Utils.YCbCrMakroBlock;

public class Frame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private boolean WRITER_ACTIVE = true;
	
	private ColorManager COLOR_MANAGER = new ColorManager();
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
	
	public int get_vec_sad_tolerance() {
		return this.ENCODE_PANEL.get_SAD_tolerance();
	}
	
	public void update_encoder_frame_count(int currentFrame, int totalFrame, boolean percentOnly) {
		this.ENCODE_PANEL.set_frame_stats(currentFrame, totalFrame, percentOnly);
		update();
	}
	
	public void update_decoder_frame_count(int currentFrame, int totalFrame, boolean percentOnly) {
		this.DECODE_PANEL.set_frame_stats(currentFrame, totalFrame, percentOnly);
		update();
	}
	
	/*
	 * Purpose: Sets the difference image in the encoding process for preview
	 * Return Type: void
	 * Params: ArrayList<MakroBlock> differences => Found differences between two frames (Without vectors);
	 * 			Dimension dim => Dimension of the difference image
	 */
	public void setDifferenceImage(ArrayList<YCbCrMakroBlock> differences, Dimension dim) {
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
	private BufferedImage render_image(ArrayList<YCbCrMakroBlock> differences, Dimension dim) {
		BufferedImage render = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		
		for (YCbCrMakroBlock block : differences) {
			int size = block.getSize();
			YCbCrColor[][] cols = block.getColors();
			
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					if (block.getPosition().x + x >= render.getWidth()
						|| block.getPosition().y + y >= render.getHeight()) {
						continue;
					}
					
					if (cols[y][x].getA() == 255) { //ASCII for YAVC
						continue;
					}
					
					render.setRGB(block.getPosition().x + x, block.getPosition().y + y, this.COLOR_MANAGER.convert_YCbCr_to_RGB(cols[y][x]).getRGB());
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
	public void set_previews(PixelRaster img1, PixelRaster img2) {
		this.ENCODE_PANEL.set_prev_frame(img1);
		this.ENCODE_PANEL.set_cur_frame(img2);
		update();
	}
	
	public void set_MBDiv_image(BufferedImage img) {
		this.ENCODE_PANEL.set_mbdiv_frame(img);
	}
	
	public void set_sobel_image(BufferedImage img) {
		this.ENCODE_PANEL.set_sobel_frame(img);
	}
	
	public void set_decoder_preview(BufferedImage img) {
		this.DECODE_PANEL.set_preview_image(img);
	}
	
	private void update() {
		this.repaint();
		this.revalidate();
	}
}
