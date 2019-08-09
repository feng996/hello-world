package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.UserOperate;

public class SelectParser {
	private ArrayList<String> columns;
	private ArrayList<String> tables;
	private Map<String, ArrayList<String>> conditions;
	private Map<String, String> replaces;
	private boolean flag;

	public ArrayList<String> getTables() {
		return tables;
	}
	
	public ArrayList<String> getColumns() {
		return columns;
	}
	
	public Map<String, ArrayList<String>> getConditions() {
		return conditions;
	}
	
	public SelectParser(String sql) throws Exception {
		flag = true;
		columns = new ArrayList<>();
		tables = new ArrayList<>();
		conditions = new HashMap<>();
		conditions.put("and", new ArrayList<String>());
		conditions.put("or", new ArrayList<String>());
		replaces = new HashMap<>();
		sql = getStandard(sql);
		sqlParser(sql);
		
//		for (String column: columns) {
//			System.out.println(column);
//		}
//		for (String table : tables) {
//			System.out.println(table);
//		}
//		System.out.println("and---------------");
//		for (String value: conditions.get("and")) {
//			System.out.println(value);
//		}
//		System.out.println("or------------------");
//		for (String value: conditions.get("or")) {
//			System.out.println(value);
//		}
		for (String tableName: tables) {
			if (tableName.contains(" ")) {
				tableName = tableName.split(" ")[0];
			}
			UserOperate u = new UserOperate(System.getProperty("cur.usr"));
			if (!u.hasThisGrant("select", tableName)) {
				System.err.println("Error: No operation permission!");
				throw new Exception();
			}
		}
	}

	public String getStandard(String sql) {
		sql = sql.trim();
		sql = sql.toLowerCase();
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.substring(0, sql.lastIndexOf(";")+1);
		sql = "" + sql;
		return sql;
	}
	
	public void sqlParser(String sql) throws Exception {
		String string = null;
		
		Pattern p = Pattern.compile("(select)(.+?)(from)");
		Matcher m = p.matcher(sql);
		if (m.find()) {
			string = m.group(2);
			if (string.contains(";")) {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
		String[] values = string.split(",");
		for (String value: values) {
			columns.add(value.trim());
		}
		
		p = Pattern.compile("(from)(.+?)(where)");
		m = p.matcher(sql);
		if (m.find()) {
			string = m.group(2);
		} else {
			p = Pattern.compile("(from)(.+)");
			m = p.matcher(sql);
			if (m.find()) {
				string = m.group(2);
			} else {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
		}
		values = string.split(",|;");
		for (String value: values) {
			tables.add(value.trim());
		}
		
		p = Pattern.compile("(where)(.+)");
		m = p.matcher(sql);
		if (m.find()) {
			string = m.group(2);
		} else {
			string = "";
		}
		
		string = change(string);
		p = Pattern.compile("(.+?)(and| or|;)");
		m = p.matcher(string);
		boolean temp = true;
		while (m.find()) {
			String oldString = m.group(1).trim();
			if (oldString.contains("condition")) {
				String tempString = oldString.substring(oldString.indexOf("condition"), oldString.length());
				oldString = oldString.replace(tempString, replaces.get(tempString));
			}
			if (temp) {
				conditions.get("and").add(oldString);
			} else {
				conditions.get("or").add(oldString);
			}
			if (m.group(2).equals("and")) {
				temp = true;
			} else {
				temp = false;
			}
		}
	}
	
	public String change(String string) {
		String newSubString = "condition";
		int countf = 1;
		int dis = 0;
		ArrayList<Integer> ar = new ArrayList<>();
		int count = 0;
		boolean flag = false;
		char[] temp = string.toCharArray();
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] == '(') {
				if (!flag) {
					ar.add(i);
					flag = true;
				}
				count++;
			}
			if (temp[i] == ')') {
				count--;
				if (flag) {
					if (count == 0) {
						ar.add(i);
						flag = false;
					}
				}
			}
		}
		if (ar.size() % 2 != 0) {
			flag = false;
			return string;
		}
		
		for (int i = 0; i < ar.size(); i+=2) {
			String tempString = string.substring(ar.get(i)-dis, ar.get(i+1)+1-dis);
			String rep = newSubString + countf;
			dis += tempString.length() - rep.length();
			string = string.replace(tempString, newSubString + countf);
			replaces.put(newSubString + countf, tempString);
			countf++;
		}
		
		return string;
	}
	
	public static void main(String... strings) throws Exception {
		Scanner in = new Scanner(System.in);
//		List<String> strs = new ArrayList<>();
//		Pattern p = Pattern.compile("(where)(.+)");
//		Matcher m = p.matcher(in.nextLine());
//		while (m.find()) {
//			strs.add(m.group(2));
//		}
//		
//		for (String str: strs) {
//			System.out.println(str);
//		}
//		String[] values = " a = 2 and 3 = 4 and (select * from tables);".split("and|;");
//		for (String value: values) {
//			System.out.println(value.trim());
//		}
//		SelectParser p = new SelectParser(in.nextLine());
//		String a = "SSN IN (SELECT ESSN FROM WORKS_ON, PROJECT WHERE PNUMBER = PNO AND PNAME ='哈同公路');".toLowerCase();
//		Pattern p = Pattern.compile("(.+?)(and|or|;)");
//		Matcher m = p.matcher("DNO = DNUMBER AND DNAME = '研发部';".toLowerCase());
//		List<String> strs = new ArrayList<>();
//		while (m.find()) {
//			strs.add(m.group(1));
//		}
////		
//		for (String str: strs) {
//			System.out.println(str);
//		}
		SelectParser p = new SelectParser(in.nextLine());
	}
}
