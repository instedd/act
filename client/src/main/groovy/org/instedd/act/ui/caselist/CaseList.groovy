package org.instedd.act.ui.caselist

import java.awt.Color;
import java.awt.Component
import java.awt.Dimension

import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
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
		"<html><div style=\"text-align: center; font-size: 1.1em;\">${text}</div></html>"
	}

	void build(List<Case> cases) {
		def container = new JPanel()
		container.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS))
		container.setPreferredSize(new Dimension(750, 480))
		
		tableModel = new CaseTableModel(columnDefinitions, toRow, cases)
		def table = new JTable(tableModel)
		table.fillsViewportHeight = true
		table.tableHeader.defaultRenderer = centeredHeaderTextRenderer()
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		table.selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				def selectedCases = table.selectedRows.collect { index ->
					table.getModel().getCase(index)
				}
				this.controller.selectedCases(selectedCases)
			}
		})
		column(table, "").preferredWidth = 5
		column(table, "Age").preferredWidth = 35
		column(table, "Gender").preferredWidth = 50
		column(table, "Follow up").cellRenderer = followUpInformationCellRenderer()
		
		def casesCount = new JLabel(" ", SwingConstants.CENTER)
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
		
		casesCount.alignmentX = Component.CENTER_ALIGNMENT
		tableModel.addTableModelListener updateCasesCountLabel
		
		updateCasesCountLabel()
		
		def gridPane = new JScrollPane(table)
		gridPane.alignmentX = Component.CENTER_ALIGNMENT
		
		def newCaseButton = new JButton("New case")
		newCaseButton.alignmentX = Component.CENTER_ALIGNMENT
		newCaseButton.addActionListener({
			controller.newCaseButtonPressed()
		})
		
		add container
		container.add casesCount
		container.add gridPane
		container.add newCaseButton
		
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
