package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.UserOperate;

public class UpdateParser {
	private ArrayList<String> tableNames;
	private ArrayList<String> setValues;
//	private ArrayList<String> conditions;
	private Map<String, String> replaces;
	private Map<String, ArrayList<String>> conditions;
	private boolean flag;
	
	public ArrayList<String> getSetValues() {
		return setValues;
	}
	
	public ArrayList<String> getTableNames() {
		return tableNames;
	}
	
	public Map<String, ArrayList<String>> getConditions() {
		return conditions;
	}
	
	public UpdateParser(String sql) throws Exception {
		sql = getStandard(sql);
		tableNames = new ArrayList<>();
		setValues = new ArrayList<>();
		conditions = new HashMap<>();
		conditions.put("and", new ArrayList<String>());
		conditions.put("or", new ArrayList<String>());
		sqlParser(sql);
		
//		for (String tableName: tableNames) {
//			System.out.println(tableName);
//		}
//		System.out.println("-------------");
//		for (String value: setValues) {
//			System.out.println(value);
//		}
//		System.out.println("-------------");
//		for (String condition: conditions.get("and")) {
//			System.out.println(condition);
//		}
//		System.out.println("-------------");
//		for (String condition: conditions.get("or")) {
//			System.out.println(condition);
//		}
		for (String tableName: tableNames) {
			if (tableName.contains(" ")) {
				tableName = tableName.split(" ")[0];
			}
			UserOperate u = new UserOperate(System.getProperty("cur.usr"));
			if (!u.hasThisGrant("update", tableName)) {
				System.err.println("Error: No operation permission!");
				throw new Exception();
			}
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
		Pattern p = Pattern.compile("update (.+?) set (.+) where (.+);");
		Matcher m = p.matcher(sql);
		
		if (m.find()) {
			for (String value: m.group(1).split(",")) {
				tableNames.add(value.trim());
			}
			for (String value: m.group(2).split(",")) {
				setValues.add(value.trim());
			}
			String string = change(m.group(3));
			p = Pattern.compile("(.+?)(and|or|;)");
			m = p.matcher(string + ";");
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
		} else {
			p = Pattern.compile("update (.+?) set (.+);");
			m = p.matcher(sql);
			
			if (m.find()) {
				for (String value: m.group(1).split(",")) {
					tableNames.add(value.trim());
				}
				for (String value: m.group(2).split(",")) {
					setValues.add(value.trim());
				}
			} else {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
		}
	}
	
	public String change(String string) {
		String newSubString = "condition";
		int countf = 1;
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
			String tempString = string.substring(ar.get(i), ar.get(i+1)+1);
			string = string.replace(tempString, newSubString + countf);
			replaces.put(newSubString + countf, tempString);
			countf++;
		}
		
		return string;
	}
	
	public static void main(String...strings) throws Exception {
//		String a = "update (.+?) set (.+) where (.+);";
//		String d = "update (.+?) set (.+);";
//		String b = "update emp a, dept c set a.sal=c.sal where a.dep=c.dep and a.sal=c.sal;";
//		String c = "update emp a, dept c set a.sal=c.sal;";
//		Pattern p = Pattern.compile(a);
//		Matcher m = p.matcher(b);
//
//		List<String> strs = new ArrayList<>();
//		while (m.find()) {
//			strs.add(m.group(3));
//		}
//		for (String str : strs) {
//			System.out.println(str);
//		}
		UpdateParser u = new UpdateParser("update emp a, dept c set a.sal=c.sal where a.dep=c.dep and a.sal=c.sal or a.sal = ssss;");
//		UpdateParser u = new UpdateParser("update emp a, dept c set a.sal=c.sal;");
	}
 }
