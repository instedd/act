package org.instedd.act.ui.caselist

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JTabbedPane;
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableColumn

import org.instedd.act.controllers.CaseListController
import org.instedd.act.models.Case
import org.instedd.act.models.CasesFile;

import sun.swing.table.DefaultTableCellHeaderRenderer

class CaseList extends JFrame {

	CaseListController controller
	CaseTableModel caseTableModel
	CaseTableModel filesTableModel
	
	def updateCasesCountLabel
	def updateMarkAsReadText
	
	def casesColumnDefinitions = [
		["", 					String.class],
		["Name", 				String.class],
		["Phone number", 		String.class],
		["Age", 				String.class],
		["Gender", 				String.class],
		["Preferred Dialect", 	String.class],
		["Reasons", 			String.class],
		["Notes", 				String.class],
		["Follow up",			String.class]
	]
	
	def caseToRow = { Case c -> [c.updated ? "*" : "", c.name, c.phone, c.age, c.gender, c.preferredDialect, c.reasons.join(", "), c.notes, c.followUpLabel()] }
	
	CaseList(CaseListController controller) {
		this.controller = controller
		this.title = "ACT - Assisted Contact Tracing"
		this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
	}
	
	def centerText(text) {
		"<html><div style=\"font-size: 1.1em;\">${text}</div></html>"
	}

	void build(List<Case> cases, List<CasesFile> files) {
		def casesContainer = new JPanel()
		casesContainer.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
		casesContainer.setLayout(new BoxLayout(casesContainer, BoxLayout.Y_AXIS))
		casesContainer.setPreferredSize(new Dimension(750, 480))
		
		caseTableModel = new CaseTableModel(casesColumnDefinitions, caseToRow, cases)
		def caseTable = new JTable(caseTableModel)
		caseTable.fillsViewportHeight = true
		caseTable.tableHeader.defaultRenderer = centeredHeaderTextRenderer()
		
		def casesCount = new JLabel(" ")
		def selectedCount = new JButton(" ")
		selectedCount.addActionListener { event ->
			def readIndexes = caseTable.selectedRows
			if(!readIndexes) {
				if(caseTable.rowCount > 0) {
					readIndexes = 0 .. (caseTable.rowCount - 1)
				} else {
					readIndexes = []
				}
			}
			def selectedCases = readIndexes.collect { index ->
				caseTableModel.getCase(index)
			}
			this.controller.markCasesAsRead(selectedCases)
		}
		
		updateCasesCountLabel = { event ->
			def updatesCount = caseTableModel.updatesCount()
			if (updatesCount == 1) {
				casesCount.text = centerText("There is <b>${updatesCount}</b> case with unread updates")
			} else if(updatesCount > 1) {
				casesCount.text = centerText("There are <b>${updatesCount}</b> cases with unread updates")
			} else {
				casesCount.text = centerText("There are no cases with unread updates")
			}
		}
		
		casesContainer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				caseTable.clearSelection()
			}
			
		})
		
		updateMarkAsReadText = { event ->
			def selectedRowsCount = caseTable.selectedRowCount
			if (selectedRowsCount == 1) {
				selectedCount.text = "<html>Mark <b>1</b> selected case as read</html>"
			} else if(selectedRowsCount > 1) {
				selectedCount.text = "<html>Mark <b>${selectedRowsCount}</b> selected cases as read</html>"
			} else {
				selectedCount.text = "<html>Mark <b>all cases</b> as read</html>"
			}
		}
		
		caseTable.selectionModel.addListSelectionListener updateMarkAsReadText
		updateMarkAsReadText()
		
		column(caseTable, "", casesColumnDefinitions).preferredWidth = 5
		column(caseTable, "Age", casesColumnDefinitions).preferredWidth = 35
		column(caseTable, "Gender", casesColumnDefinitions).preferredWidth = 50
		column(caseTable, "Follow up", casesColumnDefinitions).preferredWidth = 150
		column(caseTable, "Follow up", casesColumnDefinitions).cellRenderer = followUpInformationCellRenderer()
		
		casesCount.alignmentX = Component.CENTER_ALIGNMENT
		caseTableModel.addTableModelListener updateCasesCountLabel
		
		updateCasesCountLabel()
		
		def gridPane = new JScrollPane(caseTable)
		gridPane.alignmentX = Component.CENTER_ALIGNMENT
		
		def newCaseButton = new JButton("<html><b>New case</b></html>")
		newCaseButton.alignmentX = Component.CENTER_ALIGNMENT
		newCaseButton.addActionListener({
			controller.newCaseButtonPressed()
		})
		
		def casesFileChooser = new JFileChooser()
		casesFileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
		def uploadCasesFileButton = new JButton("<html><b>Upload</b> cases file</html>")
		uploadCasesFileButton.alignmentX = Component.CENTER_ALIGNMENT
		uploadCasesFileButton.addActionListener({
			switch(casesFileChooser.showOpenDialog(this)) {
				case JFileChooser.APPROVE_OPTION:
					controller.syncCasesFile(casesFileChooser.getSelectedFile())
					break;
				case JFileChooser.CANCEL_OPTION:
				case JFileChooser.ERROR_OPTION:
				// TODO: error handling
					break;
			}
			
		})
		
		def unreadButtonsPanel = new JPanel()
		def showUnreadButtons = new ButtonGroup()

		def button = new JRadioButton("Show all")
		button.selected = true
		showUnreadButtons.add(button)
		button.addActionListener {
			controller.onlyShowUnread = false
		}
		unreadButtonsPanel.add button
		
		button = new JRadioButton("Only unread")
		showUnreadButtons.add(button)
		button.addActionListener {
			controller.onlyShowUnread = true
		}
		unreadButtonsPanel.add button
		
		def topBar = new JPanel()
		topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS))
		topBar.border = BorderFactory.createEmptyBorder(0, 0, 10, 0)
		
		def bottomBar = new JPanel()
		bottomBar.setLayout(new BoxLayout(bottomBar, BoxLayout.X_AXIS))
		bottomBar.border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
		
		def filesContainer = new JPanel()
		filesContainer.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
		filesContainer.setLayout(new BoxLayout(filesContainer, BoxLayout.Y_AXIS))
		filesContainer.setPreferredSize(new Dimension(750, 480))
		
		def filesHeader = new JLabel(centerText("Click the upload button to import a file with cases"))
		
		def filesColumnDefinitions = [
			["Name", 				String.class],
			["Path",		 		String.class],
			["GUID", 				String.class],
			["Status", 				String.class]
		]
		
		def fileToRow = { CasesFile f ->
			String status = "UNKNOWN"
			switch(f.status) {
				case CasesFile.Status.WAITING_UPLOAD:
					status = "Waiting for upload"
					break
				case CasesFile.Status.UPLOADED:
					status = "Uploaded"
					break
				case CasesFile.Status.PROCESSING:
					status = "Processing"
					break
				case CasesFile.Status.IMPORTED:
					status = "Import done"
					break
				case CasesFile.Status.ERROR:
					status = "Error"
					break
			}
			[f.name, f.path, f.guid[0..7], status]
		}
		
		
		filesTableModel = new CaseTableModel(filesColumnDefinitions, fileToRow, files)
		def filesTable = new JTable(filesTableModel)
		filesTable.fillsViewportHeight = true
		filesTable.tableHeader.defaultRenderer = centeredHeaderTextRenderer()
		
		column(filesTable, "Name", filesColumnDefinitions).preferredWidth = 140
		column(filesTable, "Path", filesColumnDefinitions).preferredWidth = 140
		column(filesTable, "GUID", filesColumnDefinitions).preferredWidth = 20
		column(filesTable, "Status", filesColumnDefinitions).preferredWidth = 40
		
		def filesGridPane = new JScrollPane(filesTable)
		filesGridPane.alignmentX = Component.CENTER_ALIGNMENT
		
		filesContainer.add filesHeader
		filesContainer.add uploadCasesFileButton
		filesContainer.add filesGridPane
		
		
		JTabbedPane tabs = new JTabbedPane()
		tabs.addTab("Cases", casesContainer)
		tabs.addTab("Files", filesContainer)
		
		add tabs
		casesContainer.add casesCount
		casesContainer.add topBar
		topBar.add unreadButtonsPanel
		topBar.add Box.createHorizontalGlue()
		topBar.add selectedCount
		topBar.add newCaseButton
		casesContainer.add gridPane
		casesContainer.add bottomBar
		
		pack()
		setLocationRelativeTo(null)
		visible = true
	}

	TableColumn column(JTable table, String name, columnDefinitions) {
		table.columnModel.getColumn(columnIndex(name, columnDefinitions))
	}
	
	int columnIndex(String name, columnDefinitions) {
		for(int i = 0; i < columnDefinitions.size; i++) {
		    if (columnDefinitions[i][0] == name) {
		        return i
		    }
		}
		throw new IllegalArgumentException("Unknown column name!")
	}
	
	void updateCases(List<Case> cases) {
		caseTableModel.updateCases(cases)
	}
	
	void updateFiles(List<CasesFile> files) {
		filesTableModel.updateCases(files)
	}
		
	def centeredHeaderTextRenderer() {
		def renderer = new DefaultTableCellHeaderRenderer()
		renderer.setHorizontalAlignment(JLabel.CENTER)
		renderer
	}
	
	def followUpInformationCellRenderer() {
		def renderer = new DefaultTableCellRenderer()
		renderer.setHorizontalAlignment(JLabel.CENTER)
		renderer
	}
}
