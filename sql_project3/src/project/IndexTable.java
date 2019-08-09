package project;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import handle.Table;

public class IndexTable {
	private String name;
	private String tableName;
	private String columnName;
	private Map<String, Integer> index;
	
	public String getName() {
		return name;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public Map<String, Integer> getIndex() {
		return index;
	}
	
	public IndexTable(String name, String tableName, String columnName) throws Exception {
		String path = System.getProperty("cur.dir") + "\\" + System.getProperty("cur.database") + ".dictionary";
		this.name = name;
		this.tableName = tableName;
		this.columnName = columnName;
//		FileWriter fw = new FileWriter(path, true);
//		fw.write(name + "," + tableName + "," + columnName + "\r\n");
//		fw.close();
		reChange();
	}
	
	public IndexTable(String name, String tableName, String columnName, String _) throws Exception {
		index = new HashMap<>();
		this.name = name;
		this.tableName = tableName;
		this.columnName = columnName;
		loadIndex();
	}
	
	public void loadIndex() throws Exception {
		String path = System.getProperty("cur.dir") + "\\" + name + ".idx";
		Scanner in = new Scanner(Paths.get(path));
		
		while (in.hasNext()) {
			String line = in.nextLine();
			if (line.trim().equals("")) {
				continue;
			}
			
			String[] values = line.split(",");
			index.put(values[0].trim(), Integer.parseInt(values[1].trim()));
		}
		in.close();
	}
	
	public void reChange() throws Exception {
		ArrayList<String> columnsSort = new DataTable(tableName).getColumnsSort();
		int ind = columnsSort.indexOf(columnName);
		
		String path = System.getProperty("cur.dir") + "\\" + tableName + ".data";
		Scanner in = new Scanner(Paths.get(path));
		index = new HashMap<>();
		int count = 1;
		while (in.hasNext()) {
			String value = in.nextLine().split(",")[ind];
			index.put(value.trim(), count++);
		}
		path = System.getProperty("cur.dir") + "\\" + name + ".idx";
		
		FileWriter fw = new FileWriter(path, false);
		for (Map.Entry<String, Integer> kv: index.entrySet()) {
			fw.write(kv.getKey() + "," + kv.getValue() + "\r\n");
		}
		fw.close();
	}
	
}
