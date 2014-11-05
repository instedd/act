package org.instedd.act.ui

import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

class NewCaseForm extends JFrame {

	NewCaseForm() {
		title = "New case"
		defaultCloseOperation = JFrame.EXIT_ON_CLOSE
		
		def panel = new JPanel()
		panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		panel.add(new JLabel("This screen will let the user enter\na information for new cases"))
		
		add(panel)
		pack()
	}
	
}
