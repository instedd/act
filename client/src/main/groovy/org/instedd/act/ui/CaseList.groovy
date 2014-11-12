package org.instedd.act.ui

import javax.swing.JFrame
import javax.swing.JTable
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel

class CaseList extends JFrame {

	CaseList() {
		this.title = "Cases"
		this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
	}

	void build(List<String> rootLocations) {
		def grid = new JTable(new CaseTableModel())
		add(grid)
		
		pack()
		visible = true
	}

	static class CaseTableModel implements TableModel {

		@Override
		public int getRowCount() {
			return 2;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			def names = ["Column A", "Column B"]
			return names[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return Object.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			def data = [["Foo", "Foo1"],
						["Bar", "Bar2"]]  
			return data[rowIndex][columnIndex];
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}
		
	}
		
}
