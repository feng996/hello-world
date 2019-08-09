package handle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import parser.UpdateParser;
import project.DataTable;

public class UpdateHandle {
	private Map<String, ArrayList<String>> tableColumns;
	private Map<String, ArrayList<String>> conditions;
	private ArrayList<String[]> setValues;
	private ArrayList<String> tableNames;
	private Table dataTable;
	private UpdateParser parser;

	public UpdateHandle(String sql) throws Exception {
		dataTable = null;
		setValues = new ArrayList<>();
		parser = new UpdateParser(sql);
		tableColumns = new HashMap<>();
		tableNames = parser.getTableNames();
		conditions = parser.getConditions();
		for (String value : parser.getSetValues()) {
			setValues.add(value.split("="));
		}
		for (String table : tableNames) {
			Map<String, String> name = new HashMap<>();
			String tableName = table;
			if (table.contains(" ")) {
				name.put(table.split(" ")[1], table.split(" ")[0]);
				tableName = table.split(" ")[0];
			} else {
				name.put(table, table);
			}
			tableColumns.put(tableName, new DataTable(tableName).getColumnsSort());
			if (dataTable == null) {
				dataTable = new Table(tableName, name, null);
			} else {
				dataTable.multiply(new Table(tableName, name, null));
			}
		}
		dataTable.updateSelected(setValues, conditions);
		for (Map.Entry<String, ArrayList<String>> kv : tableColumns.entrySet()) {
			Table temp = (Table) dataTable.clone();
			temp.projection(kv.getValue());
			temp.saveChange(kv.getKey());
		}
//		dataTable.show();
//		for (String[] a : setValues) {
//			System.out.println(a[0] + " = " + a[1]);
//		}
//		System.out.println("and---------------");
//		for (String value : conditions.get("and")) {
//			System.out.println(value);
//		}
//		System.out.println("or------------------");
//		for (String value : conditions.get("or")) {
//			System.out.println(value);
//		}
//		dataTable.updateSelected(setValues, conditions);
//		System.out.println("\n");
//		dataTable.show();
	}

	public static void main(String... strings) throws Exception {
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		UpdateHandle u = new UpdateHandle(
				"update student, teacher set studentnum=studentnum*2;");
	}
}
