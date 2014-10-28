package org.instedd.act.ui

import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints as GBC
import java.awt.GridBagLayout

import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

import org.instedd.act.controllers.RegistrationController


class RegistrationForm extends JFrame {

	RegistrationController controller
	
	JPanel fieldsContainer
	JTextField organizationInput
	List<JComboBox> locationSelectors = []
	JLabel errorLabel
	
	RegistrationForm(RegistrationController controller) {
		this.controller = controller
		this.title = "User registration"
		this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
		this.resizable = false
	}

	void build(List<String> rootLocations) {
		add(createForm(rootLocations))
		pack()
		visible = true
	}
	
	JPanel createForm(rootLocations) {
		def form = new JPanel(new GridBagLayout())
		form.setPreferredSize(new Dimension(350, 220))
		
		//----- intro text
		def c = new GBC()
		c.gridy = 0
		c.anchor = GBC.PAGE_START
		c.weighty = 10
		c.fill = GBC.HORIZONTAL
		c.ipady = 10
		c.ipadx = 5
		form.add(new JLabel("<html><p>Please enter your organization information before <br> uploading case information</p></html>"), c)
		
		
		//----- fields
		c = new GBC()
		c.gridy = 1
		c.anchor = GBC.NORTH
		c.weighty = 80

		fieldsContainer = new JPanel(new GridBagLayout())
		addField "Organization", createOrganizationInput()
		addField "Location", 	 createLocationSelector(rootLocations)
		form.add fieldsContainer, c
		
		//----- errors 
		c = new GBC()
		c.gridy = 2
		c.weighty = 5
		c.anchor = GBC.CENTER
		
		errorLabel = new JLabel("")
		form.add(errorLabel, c)
		
		//----- submit
		c = new GBC()
		c.gridy = 3
		c.weighty = 5
		c.anchor = GBC.PAGE_END
		def submitButton = new JButton("Register")
		submitButton.addActionListener { controller.submit() }
		form.add submitButton, c
		
		form
	}
	
	JComponent createOrganizationInput() {
		organizationInput = new JTextField(20)
	}
	
	JComboBox createLocationSelector(locations) {
		def selector = new JComboBox(new Vector([""] + locations))
		locationSelectors.add(selector)
		selector.addActionListener { 
			def level = locationSelectors.indexOf(selector)
			if (selector.selectedItem == "") {
				controller.locationCleared(level)
			} else {
				controller.locationChosen(level)
			}
		}
		selector
	}

	void addField(labelText, input) {
		def c = new GBC()
		c.gridwidth = GBC.RELATIVE
		c.anchor = GBC.WEST
		c.ipady = 10
		c.ipadx = 5
		
		def label = new JLabel(labelText)
		fieldsContainer.add(label, c)

		c = new GBC()
		c.gridwidth = GBC.REMAINDER
		c.fill = GBC.HORIZONTAL
	
		fieldsContainer.add(input, c)
		label.setLabelFor(input)
				 
	}

	void removeLocationSelectorsAboveLevel(level) {
		locationSelectors.eachWithIndex { e, i ->
			if (i > level) {
				fieldsContainer.remove(e)
			}
		}
		locationSelectors = locationSelectors.take(level + 1)
		fieldsContainer.updateUI()
	}
	
	void addLocationSelector(children) {
		def c = new GBC()
		c.gridx = 1
		c.fill = GBC.HORIZONTAL
		def selector = createLocationSelector(children)
		
		fieldsContainer.add(selector, c)
		fieldsContainer.updateUI()
	}
	
	String[] locationPathUntilLevel(int level) {
		locationPath.take(level + 1)
	}
	
	String getOrganizationName() {
		organizationInput.text
	}
	
	String[] getLocationPath() {
		locationSelectors.collect { selector -> selector.selectedItem }
	}
	
	void displayError(String error) {
		errorLabel.setText(error)
	}
	
	void clearError() {
		errorLabel.setText("")
	}
}
