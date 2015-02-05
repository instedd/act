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
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableColumn

import org.instedd.act.controllers.CaseListController
import org.instedd.act.models.Case

import sun.swing.table.DefaultTableCellHeaderRenderer

class CaseList extends JFrame {

	CaseListController controller
	CaseTableModel tableModel
	
	def updateCasesCountLabel
	def updateMarkAsReadText
	
	def columnDefinitions = [
		["", 					String.class],
		["Name", 				String.class],
		["Phone number", 		String.class],
		["Age", 				String.class],
		["Gender", 				String.class],
		["Preferred Dialect", 	String.class],
		["Reasons", 			String.class],
		["Notes", 				String.class],
		["Follow up",			String.class],
		["Uploaded",			String.class]
	]
	
	def toRow = { Case c -> [c.updated ? "*" : "", c.name, c.phone, c.age, c.gender, c.preferredDialect, c.reasons.join(", "), c.notes, c.followUpLabel(), c.synced ? "Yes" : ""] }
	
	CaseList(CaseListController controller) {
		this.controller = controller
		this.title = "Cases"
		this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
	}
	
	def centerText(text) {
		"<html><div style=\"font-size: 1.1em;\">${text}</div></html>"
	}

	void build(List<Case> cases) {
		def container = new JPanel()
		container.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS))
		container.setPreferredSize(new Dimension(750, 480))
		
		tableModel = new CaseTableModel(columnDefinitions, toRow, cases)
		def table = new JTable(tableModel)
		table.fillsViewportHeight = true
		table.tableHeader.defaultRenderer = centeredHeaderTextRenderer()
		
		def casesCount = new JLabel(" ")
		def selectedCount = new JButton(" ")
		selectedCount.addActionListener { event ->
			def readIndexes = table.selectedRows
			if(!readIndexes) {
				if(table.rowCount > 0) {
					readIndexes = 0 .. (table.rowCount - 1)
				} else {
					readIndexes = []
				}
			}
			def selectedCases = readIndexes.collect { index ->
				tableModel.getCase(index)
			}
			this.controller.markCasesAsRead(selectedCases)
		}
		
		updateCasesCountLabel = { event ->
			def updatesCount = tableModel.updatesCount()
			if (updatesCount == 1) {
				casesCount.text = centerText("There is <b>${updatesCount}</b> case with unread updates")
			} else if(updatesCount > 1) {
				casesCount.text = centerText("There are <b>${updatesCount}</b> cases with unread updates")
			} else {
				casesCount.text = centerText("There are no cases with unread updates")
			}
		}
		
		container.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				table.clearSelection()
			}
			
		})
		
		updateMarkAsReadText = { event ->
			def selectedRowsCount = table.selectedRowCount
			if (selectedRowsCount == 1) {
				selectedCount.text = "<html>Mark <b>1</b> selected case as read</html>"
			} else if(selectedRowsCount > 1) {
				selectedCount.text = "<html>Mark <b>${selectedRowsCount}</b> selected cases as read</html>"
			} else {
				selectedCount.text = "<html>Mark <b>all cases</b> as read</html>"
			}
		}
		
		table.selectionModel.addListSelectionListener updateMarkAsReadText
		updateMarkAsReadText()
		
		column(table, "").preferredWidth = 5
		column(table, "Age").preferredWidth = 35
		column(table, "Gender").preferredWidth = 50
		column(table, "Follow up").preferredWidth = 150
		column(table, "Follow up").cellRenderer = followUpInformationCellRenderer()
		
		casesCount.alignmentX = Component.CENTER_ALIGNMENT
		tableModel.addTableModelListener updateCasesCountLabel
		
		updateCasesCountLabel()
		
		def gridPane = new JScrollPane(table)
		gridPane.alignmentX = Component.CENTER_ALIGNMENT
		
		def newCaseButton = new JButton("<html><b>New case</b></html>")
		newCaseButton.alignmentX = Component.CENTER_ALIGNMENT
		newCaseButton.addActionListener({
			controller.newCaseButtonPressed()
		})
		
		def topButtons = new JPanel()
		topButtons.add selectedCount
		topButtons.add newCaseButton

		topButtons.setAlignmentY(TOP_ALIGNMENT)
		topButtons.setAlignmentX(RIGHT_ALIGNMENT)

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
		
		unreadButtonsPanel.setAlignmentY(TOP_ALIGNMENT)
		unreadButtonsPanel.setAlignmentX(LEFT_ALIGNMENT)

		def topBar = new JPanel()
		topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS))
		topBar.border = BorderFactory.createEmptyBorder(0, 0, 10, 0)
		
		def bottomBar = new JPanel()
		bottomBar.setLayout(new BoxLayout(bottomBar, BoxLayout.X_AXIS))
		bottomBar.border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
		
		topBar.add unreadButtonsPanel
		topBar.add Box.createHorizontalGlue()
		topBar.add topButtons
		topBar.setMaximumSize(new Dimension((int)topBar.getMaximumSize().width, (int)topBar.getPreferredSize().height))

		add container
		container.add casesCount
		container.add topBar
		container.add gridPane
		container.add bottomBar
		
		pack()
		setLocationRelativeTo(null)
		visible = true
	}

	TableColumn column(JTable table, String name) {
		table.columnModel.getColumn(columnIndex(name))
	}
	
	int columnIndex(String name) {
		for(int i = 0; i < columnDefinitions.size; i++) {
		    if (columnDefinitions[i][0] == name) {
		        return i
		    }
		}
		throw new IllegalArgumentException("Unknown column name!")
	}
	
	void updateCases(List<Case> cases) {
		tableModel.updateCases(cases)
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
