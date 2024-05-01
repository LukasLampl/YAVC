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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import Main.EntryPoint;

public class DecodePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel frameCountLabel = new JLabel("0/0 Frames");
	private JLabel previewHolderLabel = new JLabel();
	private JProgressBar bar = new JProgressBar();
	
	private ToggleButton startBtn = new ToggleButton("Start");
	private EntryPoint entryPoint = null;
	
	public DecodePanel(Frame frame, EntryPoint entryPoint) {
		this.entryPoint = entryPoint;
		setBackground(ComponentColor.DEFAULT_COLOR);
		setLayout(new BorderLayout());
		
		add(get_preview_panel(), BorderLayout.CENTER);
		add(get_control_panel(frame), BorderLayout.SOUTH);
	}
	
	private JPanel get_preview_panel() {
		JPanel holder = new JPanel();
		holder.setLayout(new BorderLayout());
		holder.setBackground(ComponentColor.DEFAULT_COLOR);
		holder.setBorder(BorderFactory.createEmptyBorder(20, 17, 20, 17));
		
		JPanel resFramePanel = create_std_panel();
		previewHolderLabel = create_std_label(null);
		resFramePanel.add(create_std_label("Preview"));
		resFramePanel.add(previewHolderLabel);
		
		holder.add(resFramePanel, BorderLayout.CENTER);
		
		return holder;
	}
	
	private JLabel create_std_label(String text) {
		JLabel l = new JLabel(text);
		l.setBackground(ComponentColor.DROP_SHADOW_COLOR);
		
		if (text != null) {
			l.setVerticalTextPosition(JLabel.TOP);
			l.setHorizontalTextPosition(JLabel.LEFT);
			l.setForeground(ComponentColor.TEXT_COLOR);
			l.setFont(new Font("Arial", Font.PLAIN, 14));
		}
		
		return l;
	}
	
	private JPanel create_std_panel() {
		JPanel p = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2D = (Graphics2D)g.create();
				g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2D.setColor(getBackground());
				g2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
				g2D.dispose();
			}
		};
		
		p.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		p.setBackground(ComponentColor.DROP_SHADOW_COLOR);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		return p;
	}
	
	private JPanel get_control_panel(Frame frame) {
		JPanel holder = new JPanel();
		holder.setLayout(new FlowLayout());
		holder.setBackground(ComponentColor.DEFAULT_COLOR);
		
		JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
		box.setBackground(ComponentColor.DEFAULT_COLOR);
		box.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
		
		this.frameCountLabel.setBackground(ComponentColor.DEFAULT_COLOR);
		this.frameCountLabel.setForeground(ComponentColor.TEXT_COLOR);
		this.frameCountLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		
		this.bar.setMinimum(0);
		this.bar.setValue(0);
		this.bar.setMaximum(100);
		this.bar.setStringPainted(true);
		this.bar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
		this.bar.setBackground(ComponentColor.SHADE_COLOR);
		this.bar.setForeground(ComponentColor.HIGHLIGHT_SHADE_COLOR);
		this.bar.setBorderPainted(false);
		
		box.add(this.frameCountLabel);
		box.add(this.bar);
		
		this.startBtn.setContentAreaFilled(false);
		this.startBtn.setFont(new Font("Arial", Font.PLAIN, 16));
		this.startBtn.setBorderPainted(false);
		this.startBtn.setBackground(ComponentColor.SUB_COLOR);
		
		this.startBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startBtn.setClicked(!startBtn.isClicked());
				
				if (startBtn.isClicked() == false) {
					startBtn.setText("Stop");
					startBtn.setBackground(ComponentColor.STO_COLOR);
					boolean succ = entryPoint.start_decoding_process(frame);
					
					if (succ == true) {
						return;
					}
				}
				
				reset_start_btn();
				entryPoint.stop_decoding_process();
			}
		});
		
		holder.add(box);
		holder.add(this.startBtn);
		
		return holder;
	}
	
	public void reset_start_btn() {
		this.startBtn.setBackground(ComponentColor.SUB_COLOR);
		this.startBtn.setText("Start");
	}
	
	public void set_frame_stats(int frame, int maxFrames, boolean percentOnly) {
		if (percentOnly == false) {
			this.frameCountLabel.setText(frame + "/" + maxFrames + " Frames");
		}
		
		double per = ((double)(frame + 1) / (double)maxFrames) * 100;
		this.bar.setValue((int)Math.round(per));
	}
	
	public void set_preview_image(BufferedImage img) {
		this.previewHolderLabel.setIcon(resize_image(img));
	}
	
	private ImageIcon resize_image(BufferedImage img) {
		float factor = (float)img.getHeight() / (float)img.getWidth();
		int width = (int)(((float)this.getWidth() / 6 * 5) - (float)this.getWidth() / 16 * 2);
		int height = (int)(factor * width);
		
		return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_FAST));
	}
}
