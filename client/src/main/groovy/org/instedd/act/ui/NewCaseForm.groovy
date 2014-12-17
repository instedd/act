package org.instedd.act.ui

import java.awt.BorderLayout
import java.awt.Button
import java.awt.Color
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.GridBagConstraints as GBC
import java.awt.GridBagLayout
import java.awt.TextField

import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingConstants
import javax.swing.text.DefaultStyledDocument

import org.instedd.act.controllers.CaseListController

import components.DocumentSizeFilter

class NewCaseForm extends JDialog {
    
    CaseListController controller
    
    TextField nameField
    TextField phoneField
    TextField ageField
    ButtonGroup genderButtons
    JComboBox dialectCombo
    Collection<JCheckBox> reasonsChecks
    JTextArea notesField
    def notesDocument
    Button addCaseButton
    JLabel messagesLabel
    
    String getPatientName() {
        this.nameField.text ?: ""
    }
    
    String getPhone() {
        this.phoneField.text ?: ""
    }
    
    String getAge() {
        this.ageField.text ?: ""
    }
    
    String getGender() {
        JRadioButton selectedGender = genderButtons.buttons.find({ button ->
            button.isSelected()
        })
        selectedGender?.text ?: ""
    }
    
    String getDialect() {
        dialectCombo.selectedItem ?: ""
    }
    
    Collection<String> getReasons() {
        Collection<JCheckBox> selectedReasons = reasonsChecks.findAll { checkbox ->
            checkbox.isSelected()
        }
        selectedReasons.collect { checkbox ->
            checkbox.text
        }
    }
    
    String getNotes() {
        notesField.text ?: ""
    }

    NewCaseForm(JFrame parentFrame, CaseListController casesController) {
		super(parentFrame, "New case", Dialog.ModalityType.DOCUMENT_MODAL)
		setLocationRelativeTo(parentFrame)
        this.controller = casesController
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        this.resizable = false
    }
    
    def addNewCase() {
        controller.newCaseSubmitted()
    }
    
	def build() {
		def container = new JPanel()
		container.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS))
		
		JPanel headerPanel = new JPanel(new BorderLayout())
		headerPanel.border = BorderFactory.createEmptyBorder(0, 0, 10, 0)
		
		headerPanel.add new JLabel("<html><div style='font-size: 1.2em;'>Register a new case</span></html>", SwingConstants.CENTER), BorderLayout.NORTH
		
		headerPanel.add new JLabel("<html><br />Please enter the following information for reporting a new case</html>", SwingConstants.LEFT)
		
		container.add headerPanel
		
		container.add createForm()
		
		messagesLabel = new JLabel("<html></html>")
		messagesLabel.setPreferredSize(new Dimension(80, 50))
		messagesLabel.alignmentX = Component.CENTER_ALIGNMENT
		container.add messagesLabel
		
		def submitButton = new JButton("<html><b>Add case</b></html>")
		submitButton.alignmentX = Component.CENTER_ALIGNMENT
		submitButton.addActionListener({ addNewCase() })
		container.add submitButton
		
		add(container)
		pack()
		visible = true
	}
	
	def createForm() {
		def form = new JPanel(new GridBagLayout())
		
		addField form, "Name", (nameField = new TextField())
		addField form, "Phone",(phoneField = new TextField())
		addField form, "Age",  (ageField = new TextField())
		addField form, "Gender", createGenderButtons()
		addField form, "Dialect preference", createDialectCombo()
		addReasonsField form
		addNotesField form

		form
	}
	
	def createDialectCombo() {
		dialectCombo = new JComboBox(controller.availableDialects())
		dialectCombo.setSelectedIndex(-1)
		dialectCombo
	}
	
	def createGenderButtons() {
		def buttonsContainer = new JPanel()
		buttonsContainer.setLayout(new BoxLayout(buttonsContainer, BoxLayout.X_AXIS))
		genderButtons = new ButtonGroup()
		["Male", "Female"].each { gender ->
			def button = new JRadioButton(gender)
			genderButtons.add(button)
			buttonsContainer.add button
		}
		buttonsContainer
	}
	
	def addField(JPanel form, String labelText, Component input) {
		def c = new GBC()
		c.gridwidth = GBC.RELATIVE
		c.anchor = GBC.WEST
		c.ipady = 10
		c.ipadx = 5
		
		def label = new JLabel(labelText)
		form.add(label, c)

		c = new GBC()
		c.weightx = 1
		c.gridwidth = GBC.REMAINDER
		c.fill = GBC.HORIZONTAL
		
		form.add(input, c)
	}
	
	def addReasonsField(JPanel form) {
		def c = new GBC()
		c.gridwidth = GBC.REMAINDER
		c.anchor = GBC.WEST
		c.ipady = 10
		c.ipadx = 40
		
		def label = new JLabel("Reasons")
		form.add(label, c)
		
		def reasonsPanel = new JPanel()
		reasonsPanel.setLayout(new BoxLayout(reasonsPanel, BoxLayout.Y_AXIS))
		reasonsChecks = []
		controller.contactReasons().each { reason ->
			JCheckBox reasonCheck = new JCheckBox(reason)
			reasonsPanel.add(reasonCheck)
			reasonsChecks.add(reasonCheck)
		}
		form.add reasonsPanel, c
	}
	
	def addNotesField(JPanel form) {
		def c = new GBC()
		c.gridwidth = GBC.REMAINDER
		c.anchor = GBC.WEST
		c.ipady = 10
		c.ipadx = 40
		
		def label = new JLabel("Notes")
		form.add label, c
		
		c.fill = GBC.BOTH
		c.weighty = 1
		notesField = new JTextArea()
		notesField.setRows(4)
		notesField.setLineWrap(true)
		notesField.setWrapStyleWord(true)
		notesDocument = new DefaultStyledDocument()
		notesDocument.setDocumentFilter(new DocumentSizeFilter(1000))
		notesField.setDocument(notesDocument)
		def scroll = new JScrollPane(notesField)
		
		form.add scroll, c
	}
	
    def clearMessage() {
        messagesLabel.text = ''
    }
    
    def displayMessage(message) {
        messagesLabel.text = "<html>${message}</html>"
    }

}
