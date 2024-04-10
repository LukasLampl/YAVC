package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import Encoder.EntryPoint;

public class EncodePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel prevFrameLabel = null;
	private JLabel curFrameLabel = null;
	private JLabel resFrameLabel = null;
	private JLabel vecFrameLabel = null;
	
	private JLabel frameCountLabel = new JLabel("0/0 Frames");
	private JProgressBar bar = new JProgressBar();
	
	public EncodePanel(Frame frame) {
		setLayout(new BorderLayout());
		setBackground(ComponentColor.DEFAULT_COLOR);
		
		add(get_content_panel(), BorderLayout.CENTER);
		add(get_control_panel(frame), BorderLayout.SOUTH);
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
		this.bar.setMaximum(110);
		this.bar.setStringPainted(true);
		this.bar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
		this.bar.setBackground(ComponentColor.SHADE_COLOR);
		this.bar.setForeground(ComponentColor.HILIGHT_SHADE_COLOR);
		this.bar.setBorderPainted(false);
		
		box.add(this.frameCountLabel);
		box.add(this.bar);
		
		JButton startBtn = new JButton("Start") {
			private static final long serialVersionUID = 1L;
			
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
		};
		
		startBtn.setContentAreaFilled(false);
		startBtn.setFont(new Font("Arial", Font.PLAIN, 16));
		startBtn.setBorderPainted(false);
		startBtn.setBackground(ComponentColor.SUB_COLOR);
		
		startBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new EntryPoint(frame);
			}
		});
		
		holder.add(box);
		holder.add(startBtn);
		
		return holder;
	}
	
	private JScrollPane get_content_panel() {
		JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		
		JPanel holder = new JPanel();
		holder.setBackground(ComponentColor.DEFAULT_COLOR);
		holder.setLayout(new GridBagLayout());
		scroll.setViewportView(holder);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 0;
		cons.insets = new Insets(7, 7, 7, 7);
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0;
		cons.weighty = 1.0;
		
		JPanel prevFramePanel = create_std_panel();
		prevFrameLabel = create_std_label(null);
		prevFramePanel.add(create_std_label("Prev frame"));
		prevFramePanel.add(prevFrameLabel);
		
		holder.add(prevFramePanel, cons);
		
		cons.gridx = 1;
		
		JPanel curFramePanel = create_std_panel();
		curFrameLabel = create_std_label(null);
		curFramePanel.add(create_std_label("Current frame"));
		curFramePanel.add(curFrameLabel);
		
		holder.add(curFramePanel, cons);
		
		cons.gridx = 0;
		cons.gridy = 1;
		
		JPanel resFramePanel = create_std_panel();
		resFrameLabel = create_std_label(null);
		resFramePanel.add(create_std_label("Residual frame"));
		resFramePanel.add(resFrameLabel);
		
		holder.add(resFramePanel, cons);
		
		cons.gridx = 1;

		JPanel vecFramePanel = create_std_panel();
		vecFrameLabel = create_std_label(null);
		vecFramePanel.add(create_std_label("Movement vectors"));
		vecFramePanel.add(vecFrameLabel);
		
		holder.add(vecFramePanel, cons);
		
		return scroll;
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
	
	public void set_frame_stats(int frame, int maxFrames) {
		this.frameCountLabel.setText(frame + "/" + maxFrames + " Frames");
		
		double per = ((double)(frame + 1) / (double)maxFrames) * 100;
		this.bar.setValue((int)Math.round(per));
	}
	
	public void set_prev_frame(BufferedImage img) {
		this.prevFrameLabel.setIcon(resize_image(img));
	}
	
	public void set_cur_frame(BufferedImage img) {
		this.curFrameLabel.setIcon(resize_image(img));
	}
	
	public void set_res_frame(BufferedImage img) {
		this.resFrameLabel.setIcon(resize_image(img));
	}
	
	public void set_vec_frame(BufferedImage img) {
		this.vecFrameLabel.setIcon(resize_image(img));
	}
	
	private ImageIcon resize_image(BufferedImage img) {
		float factor = (float)img.getHeight() / (float)img.getWidth();
		int width = (int)(((float)this.getWidth() / 2) - (float)this.getWidth() / 16 * 2);
		int height = (int)(factor * width);
		
		return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_FAST));
	}
}
