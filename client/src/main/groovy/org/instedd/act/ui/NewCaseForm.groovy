package org.instedd.act.ui

import java.awt.BorderLayout
import java.awt.Button
import java.awt.Color
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Label
import java.awt.TextField

import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
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
        JPanel panel = new JPanel()
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
        
        def subpanel = new JPanel()
        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.X_AXIS))
        subpanel.add(new Label('Name'))
        nameField = new TextField()
        subpanel.add(nameField)
        panel.add(subpanel)
        
        subpanel = new JPanel()
        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.X_AXIS))
        subpanel.add(new Label('Phone'))
        phoneField = new TextField()
        subpanel.add(phoneField)
        panel.add(subpanel)
        
        subpanel = new JPanel()
        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.X_AXIS))
        subpanel.add(new Label('Age'))
        ageField = new TextField()
        subpanel.add(ageField)
        panel.add(subpanel)
        
        subpanel = new JPanel()
        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.X_AXIS))
        subpanel.add(new Label('Gender'))
        
        genderButtons = new ButtonGroup()
        ["Male", "Female"].each { gender ->
            def button = new JRadioButton(gender)
            genderButtons.add(button)
            subpanel.add(button)
        }
        panel.add(subpanel)
        
        subpanel = new JPanel()
        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.X_AXIS))
        subpanel.add(new Label('Dialect preference'))
        
        dialectCombo = new JComboBox(Case.AVAILABLE_DIALECTS)
        dialectCombo.setSelectedIndex(-1)
        subpanel.add(dialectCombo)
        panel.add(subpanel)
        
        subpanel = new JPanel()
        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.X_AXIS))
        subpanel.add(new Label('Reason'))
        
        
        def reasonsPanel = new JPanel()
        reasonsPanel.setLayout(new BoxLayout(reasonsPanel, BoxLayout.Y_AXIS))
        reasonsChecks = []
        Case.CONTACT_REASONS.each { reason ->
            JCheckBox reasonCheck = new JCheckBox(reason)
            reasonsPanel.add(reasonCheck)
            reasonsChecks.add(reasonCheck)
        }
        subpanel.add(reasonsPanel)
        panel.add(subpanel)
        
        subpanel = new JPanel(new FlowLayout(FlowLayout.LEFT))
        subpanel.add(new JLabel("Notes"))
        panel.add(subpanel)
        
        subpanel = new JPanel()
        subpanel.setLayout(new BorderLayout(10, 10))
        
        notesField = new JTextArea()
        notesField.setRows(3)
        notesField.setLineWrap(true)
        notesField.setWrapStyleWord(true)
        notesDocument = new DefaultStyledDocument()
        notesDocument.setDocumentFilter(new DocumentSizeFilter(1000))
        notesField.setDocument(notesDocument)
        
        def scroll = new JScrollPane(notesField)
        subpanel.add(scroll)
        panel.add(subpanel)
        
        
        subpanel = new JPanel()
        addCaseButton = new Button('Add case')
        addCaseButton.addActionListener({
            addNewCase()
        })
        subpanel.add(addCaseButton)
        panel.add(subpanel)
        
        subpanel = new JPanel()
        subpanel.setLayout(new BorderLayout(10, 10))
        messagesField = new JLabel("<html></html>")
        subpanel.add(messagesField, BorderLayout.CENTER)
        subpanel.setPreferredSize(new Dimension(80, 50))
        panel.add(subpanel)
        
        add(panel)
        pack()
        this.visible = true
    }
    
    def clearMessage() {
        messagesField.text = ''
    }
    
    def displayMessage(message) {
        messagesField.text = "<html>${message}</html>"
    }

}
