package project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DataTable {
	private String name;
	private Map<String, DataColumn> columns;
	private Map<String, IndexTable> indexes;
	private ArrayList<String> columnsSort;
	private boolean flag = true;
	
	public DataTable(String name, Map<String, DataColumn> columns, 
			Map<String, IndexTable> indexes, ArrayList<String> columnsSort) {
		this.name = name;
		this.columnsSort = columnsSort;
		this.columns = columns;
		this.indexes = indexes;
	}
	
	public DataTable(String name) throws Exception {
		if (!new Data_Dictionary().hasTable(name)) {
			System.err.printf("Error: No (%s) table!\n", name);
			throw new Exception();
		}
		String path = System.getProperty("cur.dir") + "\\" + name + ".table";
		this.name = name;
		columns = new HashMap<>();
		columnsSort = new ArrayList<>();
		indexes = new HashMap<>();
		Scanner in = new Scanner(Paths.get(path));
		
		String getString = in.nextLine();
		while(in.hasNext()) {
			getString = in.nextLine();
			if (getString.equals("")) {
				continue;
			}
			if (getString.equals("Index")) {
				break;
			}
			String[] values = getString.split(" ");
			DataColumn newColumn = new DataColumn(values[0], values[1], 
					Boolean.parseBoolean(values[2]), 
					Boolean.parseBoolean(values[3]), 
					Boolean.parseBoolean(values[4]));
			columns.put(values[0], newColumn);
			columnsSort.add(values[0]);
		}

		while(in.hasNext()) {
			String line = in.nextLine().trim();
			if (line.equals("")) {
				continue;
			}
			String[] values = line.split(",");
			indexes.put(values[0], new IndexTable(values[0].trim(), this.name, values[2].trim(), null));
		}
		in.close();
	}
	
//	public void addTable() throws IOException {
//		String path = System.getProperty("cur.dir") + "\\" + name + ".table";
//		FileWriter fw = new FileWriter(path, false);
//		fw.write("Table\r\n");
//		for (String column: columnsSort) {
//			DataColumn newColumn = columns.get(column);
//			String values = newColumn.getName() + " " + newColumn.getType() + " " + 
//					newColumn.getNull() + " " + newColumn.getPrime() + " " + newColumn.getIndex() + "\r\n";
//			fw.write(values);
//		}
//		fw.write("\r\nIndex\r\n");
//		fw.close();
//	}
	
	public void modifyType(String column, String type) throws IOException {
		columns.get(column).setType(type);
		reSave();
	}
	
	public void addColumn(String column, String type) throws Exception {
		if (!columns.get(column).getIndex()) {
			System.err.printf("Error: The column (%s) is not prime!\n", column);
			throw new Exception();
		}
		if (!columns.containsKey(column)) {
			columns.put(column, new DataColumn(column, type, false, false, false));
			columnsSort.add(column);
		} else {
			System.err.printf("Error: The (%s) has aready exists!\n", column);
			throw new Exception();
		}
		reSave();
	}
	
	public void dropColumn(String column) throws Exception {
		if (columns.containsKey(column)) {
			columns.remove(column);
			columnsSort.remove(column);
		} else {
			System.err.printf("Error: No (%s) Column!\n", column);
			throw new Exception();
		}
		reSave();
	}
	
	public void changeColumn(String oldColumn, String newColumn, String type) throws Exception {
		if (oldColumn.equals(newColumn)) {
			modifyType(newColumn, type);
		} else {
			if (columns.containsKey(oldColumn)) {
				DataColumn newColumn1 = columns.get(oldColumn);
				newColumn1.setName(newColumn);
				newColumn1.setType(type);
				columns.put(newColumn, newColumn1);
				columns.remove(oldColumn);
				columnsSort.set(columnsSort.indexOf(oldColumn), newColumn);
			} else {
				System.err.printf("Error: No (%s) Column!\n", oldColumn);
				throw new Exception();
			}
			reSave();
		}
	}
	
	public void addIndex(String indexName, String column) throws Exception {
		if (hasIndex(indexName)) {
			System.err.printf("Error: The index (%s) already exists!\n", name);
			try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
			throw new Exception();
		}
		if (!hasColumn(column)) {
			System.err.printf("Error: There is no column (%s)!\n", column);
			try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
			throw new Exception();
		}
		if (!columns.get(column).getPrime()) {
			System.err.printf("Error: The column (%s) is not prime!\n", column);
			try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
			throw new Exception();
		}
 		IndexTable ind = new IndexTable(indexName, this.name, column);
		indexes.put(name, ind);
		columns.get(column).setIndex("true");
		reSave();
	}
	
	public void dropIndex(String indexName) throws Exception {
		if (!hasIndex(indexName)) {
			System.err.printf("Error: The index (%s) does not exist!\n", indexName);
			try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
			throw new Exception();
		}
		columns.get(indexes.get(indexName).getColumnName()).setIndex("false");
		indexes.remove(indexName);
		reSave();
		String path = System.getProperty("cur.dir") + "\\" + indexName + ".idx";
		File fl = new File(path);
		fl.delete();
	}
	
	
	public void reSave() throws IOException {
		String path = System.getProperty("cur.dir") + "\\" + name + ".table";
		FileWriter fw = new FileWriter(path, false);
		fw.write("Table\r\n");
		for (String column: columnsSort) {
			DataColumn newColumn = columns.get(column);
			String values = newColumn.getName() + " " + newColumn.getType() + " " + 
					newColumn.getNull() + " " + newColumn.getPrime() + " " + newColumn.getIndex() + "\r\n";
			fw.write(values);
		}
		fw.write("\r\nIndex\r\n");
		for (Map.Entry<String, IndexTable> kv: indexes.entrySet()) {
			String index = kv.getValue().getName() + "," + kv.getValue().getTableName() + "," + kv.getValue().getColumnName() + "\r\n";
			fw.write(index);
		}
		fw.close();
	}
	
	public void show() {
		System.out.println("+------------+----------+------+-----+-------+");
		System.out.println("| ColumnName | Type     | Null | Key | Index |");
		System.out.println("+------------+----------+------+-----+-------+");
		for (String column: columnsSort) {
			DataColumn col = columns.get(column);
			System.out.print("| ");
			String colName = col.getName();
			String colType = col.getType();
			String colNull = col.getNull() ? "YES": "NO";
			String colKey = col.getPrime() ? "PRI": "";
			String colInd = col.getIndex() ? "YES": "NO";
			if (colName.length() >= 10) {
				colName = colName.substring(0, 7) + "...";
			}
			System.out.printf("%-10s", colName);
			System.out.print(" | ");
			System.out.printf("%-8s", colType);
			System.out.print(" | ");
			System.out.printf("%-4s", colNull);
			System.out.print(" | ");
			System.out.printf("%-3s", colKey);
			System.out.print(" | ");
			System.out.printf("%-5s", colInd);
			System.out.println(" |");
		}
		System.out.println("+------------+----------+------+-----+-------+");
	}
	
	public boolean hasColumn(String name) {
		return columns.containsKey(name);
	}
	
	public boolean hasIndex(String name) {
		return indexes.containsKey(name);
	}
	
	public DataColumn getColumn(String name) {
		return columns.get(name);
	}
	
	public IndexTable getIndex(String name) {
		return indexes.get(name);
	}
	
	public ArrayList<String> getColumnsSort() {
		return columnsSort;
	}
	
	public Map<String, DataColumn> getColumns() {
		return columns;
	}
}
