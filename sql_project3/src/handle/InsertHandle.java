package handle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import parser.InsertParser;
import project.DataTable;

public class InsertHandle {
	private String tableName;
	private ArrayList<String> columns;
	private ArrayList<Map<String, String>> values;
	private ArrayList<String> conditions;
	private InsertParser parser;
	private boolean flag;
	
	public InsertHandle(String sql) throws Exception {
		flag = true;
		parser = new InsertParser(sql);
		tableName = parser.getTableName();
		columns = parser.getColumns();
		values = parser.getValues();
		conditions = parser.getConditions();
		addValues();
		writeTo();
	}
	
	public void addValues() throws Exception {
		for (String condition: conditions) {
			Table table = new SelectHandle(condition+";", null).handle();
			ArrayList<String> colSort = table.columnsSort;
			for (Map<String, String> kv: table.tableDatas) {
				Map<String, String> kvp = new HashMap<>();
				for (int i = 0; i < columns.size(); i++) {
					kvp.put(columns.get(i), kv.get(colSort.get(i)));
				}
				values.add(kvp);
			}
		}
	}
	
	public void writeTo() throws IOException {
		String path = System.getProperty("cur.dir") + "\\" + tableName + ".data";
		FileWriter fw = new FileWriter(path, true);
		for (Map<String, String> kv: values) {
			String value = "";
			for (String column: columns) {
				String temp = kv.get(column);
				if (temp.contains("'")) {
					temp = temp.substring(1, temp.length()-1);
				}
				value += temp + ",";
			}
			value = value.substring(0, value.length()-1);
			value += "\n";
			fw.write(value);
		}
		fw.close();
	}
	
	public static void main(String...strings) throws Exception {
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		InsertHandle i = new InsertHandle("insert into student values(1, 2), (select * from student);");
	}
}
