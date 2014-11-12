package org.instedd.act.ui.caselist

import java.awt.Component
import java.awt.Dimension

import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.TableColumn

import org.instedd.act.controllers.CaseListController
import org.instedd.act.models.Case

class CaseList extends JFrame {

	CaseListController controller
	CaseTableModel tableModel
	
	def columnDefinitions = [
		["Id", 					String.class],
		["Name", 				String.class],
		["Phone number", 		String.class],
		["Age", 				String.class],
		["Gender", 				String.class],
		["Preferred Dialect", 	String.class],
		["Reasons", 			String.class],
		["Notes", 				String.class]
	]
	
	def toRow = { Case c -> [c.id, c.name, c.phone, c.age, c.gender, c.preferredDialect, c.reasons.join(", "), c.notes] }
	
	CaseList(CaseListController controller) {
		this.controller = controller
		this.title = "Cases"
		this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
	}

	void build(List<Case> cases) {
		def container = new JPanel()
		container.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS))
		container.setPreferredSize(new Dimension(750, 450))
		
		tableModel = new CaseTableModel(columnDefinitions, toRow, cases)
		def table = new JTable(tableModel)
		table.fillsViewportHeight = true
		column(table, "Age").preferredWidth = 35
		column(table, "Gender").preferredWidth = 50
		
		
		def gridPane = new JScrollPane(table)
		gridPane.alignmentX = Component.CENTER_ALIGNMENT
		
		def newCaseButton = new JButton("New case")
		newCaseButton.alignmentX = Component.CENTER_ALIGNMENT
		newCaseButton.addActionListener({
			controller.newCaseButtonPressed()
		})
		
		add container
		container.add gridPane
		container.add newCaseButton
		
		pack()
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
}
