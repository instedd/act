package org.instedd.act.ui.caselist

import java.util.List;

import javax.swing.table.AbstractTableModel

import org.instedd.act.models.Case;


class CaseTableModel extends AbstractTableModel {

	def columnDefinitions
	def toRow
		
	Object[][] rows
	Collection<Case> cases
	
	CaseTableModel(columnDefinitions, toRow, cases) {
		this.columnDefinitions = columnDefinitions
		this.toRow = toRow
		this.rows = cases.collect toRow
		this.cases = cases
	}
	
	@Override
	int getRowCount() {
		rows.length;
	}

	@Override
	int getColumnCount() {
		columnDefinitions.size;
	}

	@Override
	String getColumnName(int columnIndex) {
		columnDefinitions[columnIndex][0];
	}
	
	@Override
	Object getValueAt(int rowIndex, int columnIndex) {
		rows[rowIndex][columnIndex];
	}
	
	@Override
	Class<?> getColumnClass(int columnIndex) {
		columnDefinitions[columnIndex][1]
	}
	
	int columnIndex(String columnname) {
		
	}
	
	void updateCases(List<Case> cases) {
		this.rows = cases.collect toRow
		this.cases = cases
		fireTableDataChanged()
	}
	
	boolean isCaseUpdated(int rowIndex) {
		cases[rowIndex].updated
	}
	
	Case getCase(int rowIndex) {
		cases[rowIndex]
	}
}
