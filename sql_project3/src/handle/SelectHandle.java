package handle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parser.SelectParser;

public class SelectHandle {
	private Table dataTable;
	private ArrayList<String> columns;
	private Map<String, Table> tables;
	private Map<String, ArrayList<String>> conditions;
	private SelectParser parser;
	private boolean flag;

	public SelectHandle(String sql, ArrayList<Map<String, String>> par) throws Exception {
		flag = true;
		parser = new SelectParser(sql);
		columns = parser.getColumns();
		conditions = parser.getConditions();
		tables = new HashMap<>();
		for (String table : parser.getTables()) {
			Map<String, String> name = new HashMap<>();
			String tableName = table;
			if (table.contains(" ")) {
				name.put(table.split(" ")[1], table.split(" ")[0]);
				tableName = table.split(" ")[0];
			} else {
				name.put(table, table);
			}
			tables.put(table, new Table(tableName, name, par));
		}
		boolean temp = true;
		for (Map.Entry<String, Table> kv : tables.entrySet()) {
			if (temp) {
				dataTable = kv.getValue();
				temp = false;
			} else {
				dataTable.multiply(kv.getValue());
			}
		}

		// dataTable.show();

	}

	public Table handle() throws Exception {
		dataTable.selectConditions(conditions);
		dataTable.projection(columns);
		return dataTable;
	}

	public static void main(String... strings) throws Exception {
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		SelectHandle s = new SelectHandle("select * from student a, teacher b where b.teacherid=10000 and b.teachername = 'qwer' and a.studentid exists (select c.studentid from student c where c.studentid = 111);", null);
		Table a = s.handle();
		a.show();
	}

}
