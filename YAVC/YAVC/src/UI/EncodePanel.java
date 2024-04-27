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
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Main.EntryPoint;
import Utils.PixelRaster;

public class EncodePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private double COLOR_DAMPING_TOLERANCE = 0.75;
	private int SAD_TOLERANCE = 32768;
	
	private JLabel prevFrameLabel = null;
	private JLabel curFrameLabel = null;
	private JLabel resFrameLabel = null;
	private JLabel vecFrameLabel = null;
	
	private JLabel frameCountLabel = new JLabel("0/0 Frames");
	private JProgressBar bar = new JProgressBar();
	
	private ToggleButton startBtn = new ToggleButton("Start");
	private EntryPoint entryPoint = null;
	
	public EncodePanel(Frame frame, EntryPoint entryPoint) {
		this.entryPoint = entryPoint;
		setLayout(new BorderLayout());
		setBackground(ComponentColor.DEFAULT_COLOR);
		
		add(get_input_panel(), BorderLayout.NORTH);
		add(get_content_panel(), BorderLayout.CENTER);
		add(get_control_panel(frame), BorderLayout.SOUTH);
	}
	
	private JPanel get_input_panel() {
		JPanel holder = new JPanel();
		holder.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
		holder.setLayout(new GridBagLayout());
		holder.setBackground(ComponentColor.DEFAULT_COLOR);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 0;
		cons.fill = GridBagConstraints.BOTH;
		cons.anchor = GridBagConstraints.NORTH;
		cons.weightx = 1.0;
		cons.insets = new Insets(0, 4, 0, 4);
		
		Hashtable<Integer, JLabel> colDampTable = new Hashtable<Integer, JLabel>();
		colDampTable.put(0, create_label("0%"));
		colDampTable.put(50, create_label("50%"));
		colDampTable.put(100, create_label("100%"));
		
		CustomSlider colDampSlider = new CustomSlider(0, 100, (int)(this.COLOR_DAMPING_TOLERANCE * 100));
		JPanel colDampPanel = create_std_ctrl_panel("Damping equality", colDampTable, colDampSlider);
		
		colDampSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				COLOR_DAMPING_TOLERANCE = (double)colDampSlider.getValue() / 100;
			}
		});
		
		Hashtable<Integer, JLabel> maxSADTable = new Hashtable<Integer, JLabel>();
		maxSADTable.put(0, create_label("Precise"));
		maxSADTable.put(524288, create_label("Less precise"));
		maxSADTable.put(1048576, create_label("Unprecise"));
		
		CustomSlider maxSADSlider = new CustomSlider(0, 1048576, this.SAD_TOLERANCE);
		JPanel maxSADPanel = create_std_ctrl_panel("SAD tolerance", maxSADTable, maxSADSlider);
		
		maxSADSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				COLOR_DAMPING_TOLERANCE = maxSADSlider.getValue();
			}
		});
		
		holder.add(colDampPanel, cons);
		cons.gridx++;
		holder.add(maxSADPanel, cons);
		
		return holder;
	}
	
	private JPanel create_std_ctrl_panel(String desc, Hashtable<Integer, JLabel> table, CustomSlider slider) {
		JPanel holder = new JPanel();
		holder.setLayout(new BoxLayout(holder, BoxLayout.Y_AXIS));
		holder.setBackground(ComponentColor.DEFAULT_COLOR);
		
		JLabel descLabel = new JLabel(desc);
		descLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		descLabel.setForeground(ComponentColor.TEXT_COLOR);
		descLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		descLabel.setHorizontalTextPosition(JLabel.CENTER);
		
		slider.setBorder(BorderFactory.createEmptyBorder(7, 0, 0, 0));
		slider.setBackground(ComponentColor.DEFAULT_COLOR);
		slider.setPaintLabels(true);
		slider.setLabelTable(table);
		
		holder.add(descLabel);
		holder.add(slider);
		return holder;
	}
	
	private JLabel create_label(String text) {
		JLabel l = new JLabel(text);
		l.setFont(new Font("Arial", Font.PLAIN, 12));
		l.setForeground(ComponentColor.TEXT_COLOR);
		l.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		return l;
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
					boolean succ = entryPoint.start_encode(frame);
					
					if (succ == true) {
						return;
					}
					
					startBtn.setClicked(!startBtn.isClicked());
				}
				
				reset_start_btn();
				entryPoint.stop_encoding_process();
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
	
	private JScrollPane get_content_panel() {
		JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
		
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
		prevFramePanel.add(create_std_label("Prev frame (P-Frame)"));
		prevFramePanel.add(prevFrameLabel);
		
		holder.add(prevFramePanel, cons);
		
		cons.gridx = 1;
		
		JPanel curFramePanel = create_std_panel();
		curFrameLabel = create_std_label(null);
		curFramePanel.add(create_std_label("Current frame (I-Frame)"));
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
	
	public void set_frame_stats(int frame, int maxFrames, boolean percentOnly) {
		if (percentOnly == false) {
			this.frameCountLabel.setText(frame + "/" + maxFrames + " Frames");
		}
		
		double per = ((double)(frame + 1) / (double)maxFrames) * 100;
		this.bar.setValue((int)Math.round(per));
	}
	
	public void set_prev_frame(PixelRaster img) {
		this.prevFrameLabel.setIcon(resize_image(img));
	}
	
	public void set_cur_frame(PixelRaster img) {
		this.curFrameLabel.setIcon(resize_image(img));
	}
	
	public void set_res_frame(BufferedImage img) {
		this.resFrameLabel.setIcon(resize_image(img));
	}
	
	public void set_vec_frame(BufferedImage img) {
		this.vecFrameLabel.setIcon(resize_image(img));
	}
	
	private ImageIcon resize_image(PixelRaster img) {
		float factor = (float)img.getHeight() / (float)img.getWidth();
		int width = (int)(((float)this.getWidth() / 2) - (float)this.getWidth() / 16 * 2);
		int height = (int)(factor * width);
		
		BufferedImage rep = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				rep.setRGB(x, y, new Color(img.getRGB(x, y)).getRGB());
			}
		}
		
		return new ImageIcon(rep.getScaledInstance(width, height, Image.SCALE_FAST));
	}
	
	private ImageIcon resize_image(BufferedImage img) {
		float factor = (float)img.getHeight() / (float)img.getWidth();
		int width = (int)(((float)this.getWidth() / 2) - (float)this.getWidth() / 16 * 2);
		int height = (int)(factor * width);
		
		return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_FAST));
	}
}
