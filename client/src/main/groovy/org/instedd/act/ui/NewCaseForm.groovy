package org.instedd.act.ui

import java.awt.BorderLayout
import java.awt.Button
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
import javax.swing.text.DefaultStyledDocument

import org.instedd.act.controllers.CaseListController
import org.instedd.act.models.Case

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
    JLabel messagesField
    
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
		
		def form = createForm()
		container.add form
		
		def messages = new JPanel()
		messages.setPreferredSize(new Dimension(80, 50))
		messages.setLayout(new BorderLayout(10, 10))
		messagesField = new JLabel("<html></html>")
		messages.add(messagesField)
		container.add messages
		
		def submitButton = new JButton("Add case")
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
		Case.CONTACT_REASONS.each { reason ->
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
		notesField = new JTextArea()
		notesField.setRows(3)
		notesField.setLineWrap(true)
		notesField.setWrapStyleWord(true)
		notesDocument = new DefaultStyledDocument()
		notesDocument.setDocumentFilter(new DocumentSizeFilter(1000))
		notesField.setDocument(notesDocument)
		def scroll = new JScrollPane(notesField)
		
		form.add scroll, c
	}
	
    def clearMessage() {
        messagesField.text = ''
    }
    
    def displayMessage(message) {
        messagesField.text = "<html>${message}</html>"
    }

}
