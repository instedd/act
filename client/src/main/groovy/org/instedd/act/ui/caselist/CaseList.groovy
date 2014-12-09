package org.instedd.act.ui.caselist

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
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
	
	def columnDefinitions = [
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
	
	def toRow = { Case c -> [c.updated ? "*" : "", c.name, c.phone, c.age, c.gender, c.preferredDialect, c.reasons.join(", "), c.notes, c.followUpLabel()] }
	
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
			def seenIndexes = table.selectedRows ?: 0 .. (table.rowCount - 1)
			def selectedCases = seenIndexes.collect { index ->
				tableModel.getCase(index)
			}
			this.controller.markCasesAsSeen(selectedCases)
		}
		
		def updateCasesCountLabel = { event ->
			def updatesCount = tableModel.updatesCount()
			if (updatesCount == 1) {
				casesCount.text = centerText("There is <b>${updatesCount}</b> case with updates")
			} else if(updatesCount > 1) {
				casesCount.text = centerText("There are <b>${updatesCount}</b> cases with updates")
			} else {
				casesCount.text = centerText("There are no cases with updates")
			}
		}
		
		container.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				table.clearSelection()
			}
			
		})
		
		def updateMarkAsSeenText = { event ->
			def selectedRowsCount = table.selectedRowCount
			if (selectedRowsCount == 1) {
				selectedCount.text = "<html>Mark <b>1</b> selected case as seen</html>"
			} else if(selectedRowsCount > 1) {
				selectedCount.text = "<html>Mark <b>${selectedRowsCount}</b> selected cases as seen</html>"
			} else {
				selectedCount.text = "<html>Mark <b>all cases</b> as seen</html>"
			}
		}
		
		table.selectionModel.addListSelectionListener updateMarkAsSeenText
		updateMarkAsSeenText()
		
		column(table, "").preferredWidth = 5
		column(table, "Age").preferredWidth = 35
		column(table, "Gender").preferredWidth = 50
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
		
		def topBar = new JPanel()
		topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS))
		topBar.border = BorderFactory.createEmptyBorder(0, 0, 10, 0)
		
		add container
		container.add topBar
		topBar.add casesCount
		topBar.add selectedCount
		topBar.add newCaseButton
		container.add gridPane
		
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
