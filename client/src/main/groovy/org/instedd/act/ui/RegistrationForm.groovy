package org.instedd.act.ui

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component;
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints as GBC
import java.awt.GridBagLayout
import java.awt.GridLayout

import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants

import org.instedd.act.controllers.RegistrationController
import org.instedd.act.models.Location


class RegistrationForm extends JFrame {

	RegistrationController controller
	
	JPanel fieldsContainer
	JTextField organizationInput
	JLabel errorLabel
	JTextField fieldSupervisorNameInput
	JTextField fieldSupervisorNumberInput
	JTextField locationInput
	
	JScrollPane locationListScroll
	JList locationList
	
	RegistrationForm(RegistrationController controller) {
		this.controller = controller
		this.title = "Device registration"
		this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
		this.resizable = false
	}

	void build() {
		add(createForm())
		pack()
		setLocationRelativeTo(null)
		visible = true
	}
	
	JPanel createForm() {
		def form = new JPanel(new GridBagLayout())
		form.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		form.setPreferredSize(new Dimension(500, 400))
		
		//----- intro text
		def c = new GBC()
		c.gridy = 0
		c.anchor = GBC.NORTH
		c.fill = GBC.HORIZONTAL
		c.ipady = 10
		c.ipadx = 5
		form.add(new JLabel("<html><div style='font-size: 1.2em;'>Welcome to ACT</span></html>", SwingConstants.CENTER), c)
		
		c = new GBC()
		c.gridy = 1
		c.anchor = GBC.PAGE_START
		c.fill = GBC.HORIZONTAL
		c.ipady = 10
		c.ipadx = 5
		form.add(new JLabel("<html>Please enter your organization information before uploading case information</html>"), c)
		
		
		//----- fields
		c = new GBC()
		c.gridy = 2
		c.anchor = GBC.NORTH
		c.fill = GBC.HORIZONTAL
		c.ipady = 10
		
		// Assigning non zero weight makes this component 
		// get all the available extra space after placing
		// components
		c.weighty = 1
		c.weightx = 1

		fieldsContainer = new JPanel(new GridBagLayout())
		addField "Organization", createOrganizationInput()
		addField "<html>Supervisor name</html>", createSupervisorNameInput()
		addField "<html>Supervisor number</html>", createSupervisorPhoneInput()
		addLocationField()
		form.add fieldsContainer, c
		
		//----- errors 
		c = new GBC()
		c.gridy = 3
		c.anchor = GBC.SOUTH
		c.fill = GBC.HORIZONTAL
		c.ipady = 10
		errorLabel = new JLabel("")
		form.add(errorLabel, c)
		
		//----- submit
		c = new GBC()
		c.fill = GBC.HORIZONTAL
		c.ipadx = 5
		c.gridy = 4
		c.anchor = GBC.PAGE_END
		
		def bottomBar = new JPanel()
		bottomBar.setLayout(new GridLayout(1, 3))
		
		bottomBar.add new JLabel("")
		
		def submitButton = new JButton("<html><b>Register</b></html>")
		submitButton.addActionListener { controller.submit() }
		bottomBar.add submitButton
		
		def versionLabel = new JLabel(controller.settings.appVersion())
		versionLabel.setForeground(Color.GRAY)
		versionLabel.setHorizontalAlignment(SwingConstants.RIGHT)
		bottomBar.add versionLabel
		
		form.add bottomBar, c
		
		this.getRootPane().setDefaultButton(submitButton)
		
		form
	}
	
	JComponent createOrganizationInput() {
		organizationInput = new JTextField(20)
	}
	
	JComponent createSupervisorNameInput() {
		fieldSupervisorNameInput = new JTextField(20)
	}
	
	JComponent createSupervisorPhoneInput() {
		fieldSupervisorNumberInput = new JTextField(20)
	}
	
	void addField(labelText, input) {
		def c = new GBC()
		c.gridwidth = GBC.RELATIVE
		c.anchor = GBC.WEST
		c.ipady = 10
		c.ipadx = 5
		
		def label = new JLabel(labelText)
		label.setPreferredSize(new Dimension(130, label.preferredSize.height.toInteger()))
		label.setMinimumSize(label.getPreferredSize())
		label.setMaximumSize(label.getPreferredSize())
		fieldsContainer.add(label, c)

		c = new GBC()
		c.gridwidth = GBC.REMAINDER
		c.fill = GBC.HORIZONTAL
		c.weightx = 1
	
		fieldsContainer.add(input, c)
		label.setLabelFor(input)
				 
	}

	void addLocationField() {
		fieldsContainer.add new JLabel("Location"), new GBC([
			gridwidth: GBC.RELATIVE,
			ipady: 10,
			ipadx: 5,
			anchor: GBC.NORTHWEST
		]);
	
		locationInput = new JTextField(20)

		locationList = new JList<String>(new DefaultListModel<>())
		locationList.selectionMode = ListSelectionModel.SINGLE_SELECTION
		locationList.layoutOrientation = JList.VERTICAL
		
		locationListScroll = new JScrollPane(locationList)
		locationListScroll.setMinimumSize(new Dimension(locationListScroll.getPreferredSize().width as Integer, 110))
		
		def locationSelector = new JPanel()
		locationSelector.setLayout(new BoxLayout(locationSelector, BoxLayout.Y_AXIS))
		
		fieldsContainer.add locationSelector, new GBC([
			gridwidth: GBC.REMAINDER,
			fill: GBC.HORIZONTAL
		])
		
		locationSelector.add(locationInput)
		locationSelector.add(locationListScroll)
		
		toggleLocationOptions(false)
	}
	
	String getOrganizationName() {
		organizationInput.text
	}
	
	String getSupervisorName() {
		fieldSupervisorNameInput.text
	}
	
	String getSupervisorNumber() {
		fieldSupervisorNumberInput.text
	}
	
	Location getSelectedLocation() {
		locationList.selectedValue
	}
	
	void displayError(String error) {
		errorLabel.setText("<html>${error}</html>")
	}
	
	void clearError() {
		errorLabel.setText("")
	}
	
	def clearLocationOptions() {
		locationList.model.clear()
	}
	
	def displayLocationOptions(options) {
		def m = new DefaultListModel()
		options.each { l -> m.addElement(l) }
		locationList.setModel(m)
		
		if (!options.empty) {
			locationList.setSelectedIndex(0)
		}
	}
	
	def toggleLocationOptions(Boolean isVisible) {
		locationListScroll.visible = isVisible
		pack()
	}

	def hideLocationOptions() {
		toggleLocationOptions(false)
	}

	def showLocationOptions() {
		toggleLocationOptions(true)
	}
	
}
