package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.UserOperate;

public class DeleteParser {
	private String tableName;
//	private ArrayList<String> conditions;
	private Map<String, ArrayList<String>> conditions;
	private Map<String, String> replaces;
	private boolean flag;
	
	public String getTableName() {
		return tableName;
	}
	
	public Map<String, ArrayList<String>> getConditions() {
		return conditions;
	}
	
	public DeleteParser(String sql) throws Exception {
		flag = true;
		conditions = new HashMap<>();
		conditions.put("and", new ArrayList<String>());
		conditions.put("or", new ArrayList<String>());
		sql = getStandard(sql);
		sqlParser(sql);
//		System.out.println(flag);
//		System.out.println(tableName);
//		System.out.println("and---------------");
//		for (String value : conditions.get("and")) {
//			System.out.println(value);
//		}
//		System.out.println("or------------------");
//		for (String value : conditions.get("or")) {
//			System.out.println(value);
//		}
		UserOperate u = new UserOperate(System.getProperty("cur.usr"));
		if (!u.hasThisGrant("delete", tableName)) {
			System.err.println("Error: No operation permission!");
			throw new Exception();
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
		Pattern p = Pattern.compile("^delete from (.+?) where (.+);$");
		Matcher m = p.matcher(sql);
		
		if (m.find()) {
			tableName = m.group(1);
			String string = m.group(2);
			string = change(string);
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
			p = Pattern.compile("^delete from ([0-9a-zA-Z_-]+?)\\s*;$");
			m = p.matcher(sql);
			if (m.find()) {
				tableName = m.group(1);
			} else {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
			
		}
	}
	
	public String change(String string) throws Exception {
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
//		String a = "^delete from (.+?)\\s*where (.+);";
//		String d = "^delete from ([0-9a-zA-Z_-]+?)\\s*;$";
//		
//		String b = "delete from emp where ename = 'dony' and edi = 123;";
//		String c = "delete from emp;";
//		Pattern p = Pattern.compile(d);
//		Matcher m = p.matcher(c);
//
//		List<String> strs = new ArrayList<>();
//		while (m.find()) {
//			strs.add(m.group(1));
//		}
//		for (String str : strs) {
//			System.out.println(str);
//		}
		DeleteParser d = new DeleteParser("delete from emp where ename = 'dony' and edi = 123 or c = 222;");
	}
}
