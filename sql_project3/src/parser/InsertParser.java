package parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.DataTable;
import project.UserOperate;

public class InsertParser {
	private String tableName;
	private ArrayList<String> columns;
	private ArrayList<ArrayList<String>> values;
	private ArrayList<Map<String, String>> column_value;
	private ArrayList<String> conditions;
	private boolean flag;

	public String getTableName() {
		return tableName;
	}
	
	public ArrayList<String> getColumns() {
		return columns;
	}
	
	public ArrayList<Map<String, String>> getValues() {
		return column_value;
	}
	
	public ArrayList<String> getConditions() {
		return conditions;
	}
	
	public InsertParser(String sql) throws Exception {
		flag = true;
		conditions = new ArrayList<>();
		columns = new ArrayList<>();
		values = new ArrayList<>();
		column_value = new ArrayList<>();
		sql = getStandard(sql);
		sqlParser(sql);
		setColumns();
		getMap();
//		System.out.println(flag);
//		System.out.println(tableName);
//		for (ArrayList<String> arr: values) {
//			for (String value: arr) {
//				System.out.print(value);
//			}
//			System.out.println();
//		}
//		
//		for (String condition: conditions) {
//			System.out.println(condition);
//		}
		UserOperate u = new UserOperate(System.getProperty("cur.usr"));
		if (!u.hasThisGrant("insert", tableName)) {
			System.err.println("Error: No operation permission!");
			throw new Exception();
		}
	}
	
	public void setColumns() throws Exception {
		if (columns.size() == 0) {
			for (String column: new DataTable(tableName).getColumnsSort()) {
				columns.add(column);
			}
		}
	}
	
	public void getMap() {
		for (ArrayList<String> ar: values) {
			Map<String, String> kv = new HashMap<>();
			for (int i = 0; i < columns.size(); i++) {
				kv.put(columns.get(i), ar.get(i));
			}
			column_value.add(kv);
		}
	}
	
	public String getStandard(String sql) {
		sql = sql.trim();
		sql = sql.toLowerCase();
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.substring(0, sql.lastIndexOf(";") + 1);
		sql = "" + sql;
		return sql;
	}

	public void sqlParser(String sql) throws Exception {
		Pattern p = Pattern.compile("insert into (.+?)\\s*\\((.+?)\\)\\s*values(.+);");
		Matcher m = p.matcher(sql);

		if (m.find()) {
			tableName = m.group(1);
			for (String column: m.group(2).split(",")) {
				columns.add(column.trim());
			}
			p = Pattern.compile("\\((.+?)\\)");
			m = p.matcher(m.group(3));
			boolean temp = false;
			while (m.find()) {
				temp = true;
				if (m.group(1).contains("select")) {
					conditions.add(m.group(1).trim());
				} else {
					ArrayList<String> newList = new ArrayList<>();
					for (String value: m.group(1).split(",")) {
						newList.add(value.trim());
					}
					values.add(newList);
				}
			}
			if (!temp) {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
		} else {
			p = Pattern.compile("insert into (.+?)\\s*values(.+);");
			m = p.matcher(sql);
			if (m.find()) {
				tableName = m.group(1);
				p = Pattern.compile("\\((.+?)\\)");
				m = p.matcher(m.group(2));
				boolean temp = false;
				while (m.find()) {
					temp = true;
					if (m.group(1).contains("select")) {
						conditions.add(m.group(1).trim());
					} else {
						ArrayList<String> newList = new ArrayList<>();
						for (String value: m.group(1).split(",")) {
							newList.add(value.trim());
						}
						values.add(newList);
					}
				}
				if (!temp) {
					System.err.println("Error: Illegal Instruction!");
					throw new Exception();
				}
			} else {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
		}
	}

	public static void main(String... strings) throws Exception {
		// String a = "insert into (.+?)\\s*values\\s*\\((.+)\\);";
		//
		// String b = "insert into (.+?)\\s*\\((.+?)\\)\\s*values\\s*\\((.+)\\);";
		//
		// String c = "insert into table1(a, b) values(select * from table);";
		// String d = "insert into table1 values(1, 2);";
		//// Pattern p = Pattern.compile(b);
		//// Matcher m = p.matcher(d);
		////
		//// List<String> strs = new ArrayList<>();
		//// while (m.find()) {
		//// strs.add(m.group(3));
		//// }
		//// for (String str: strs) {
		//// System.out.println(str);
		//// }
		//
		// String e = "a, b";
		// String[] es = e.split(",");
		// for (String temp: es) {
		// System.out.println(temp);
		// }
//		String b = "insert into (.+?)\\s*\\((.+?)\\)\\s*values(.+);";
//		Pattern p = Pattern.compile(b);
//		Matcher m = p.matcher("insert into table1 values(1, 2), (3, 4);");
//
//		List<String> strs = new ArrayList<>();
//		while (m.find()) {
//			strs.add(m.group(1));
//		}
//		for (String str : strs) {
//			System.out.println(str);
//		}
		InsertParser i = new InsertParser("insert into table1 values(1, 2), (3, 4), (select * from table2);");
//		String b = "\\((.+?)\\)";
//		Pattern p = Pattern.compile(b);
//		Matcher m = p.matcher("(1, 2), (3, 4)");
//
//		List<String> strs = new ArrayList<>();
//		while (m.find()) {
//			strs.add(m.group(1));
//		}
//		for (String str : strs) {
//			System.out.println(str);
//		}
	}
}
