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
