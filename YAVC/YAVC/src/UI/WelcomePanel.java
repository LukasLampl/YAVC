package UI;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class WelcomePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public WelcomePanel() {
		setBackground(ComponentColor.DEFAULT_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 20, 0, 0));

		JLabel welcomeLabel = new JLabel("Hello, " + System.getProperty("user.name") + "!");
		welcomeLabel.setForeground(ComponentColor.HIGHLIGHT_TEXT_COLOR);
		welcomeLabel.setBackground(ComponentColor.DEFAULT_COLOR);
		welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
		welcomeLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		add(welcomeLabel);

		JLabel textLabel = new JLabel("<html>It's a pleasure to see you!<br>What do you want to do?</html>");
		textLabel.setForeground(ComponentColor.TEXT_COLOR);
		textLabel.setBackground(ComponentColor.DEFAULT_COLOR);
		textLabel.setFont(new Font("Arial", Font.PLAIN, 20));
		textLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		textLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
		add(textLabel);
	}
}
