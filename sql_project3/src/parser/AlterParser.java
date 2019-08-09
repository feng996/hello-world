package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlterParser {
	private String tableName;
	private String type;
	private Map<String, Map<String, String>> conditions;
	private boolean flag;

	public AlterParser(String sql) throws Exception {
		flag = true;
		conditions = new HashMap<>();
		sql = getStandard(sql);
		sqlParser(sql);
//		System.out.println(flag);
//		System.out.println(tableName);
//		System.out.println(type);
//		System.out.println("------------------");
//		for (Map.Entry<String, Map<String, String>> kvp: conditions.entrySet()) {
//			System.out.println(kvp.getKey());
//			for (Map.Entry<String, String> kv: kvp.getValue().entrySet()) {
//				System.out.println(kv.getKey() + ", " + kv.getValue());
//			}
//		}
		
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getType() {
		return type;
	}
	
	public Map<String, String> getConditions() {
		return conditions.get(type);
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
		Pattern p = Pattern.compile("alter table (.+?) (modify|add|drop|change|rename) (.+);");
		Matcher m = p.matcher(sql);
		if (m.find()) {
			tableName = m.group(1).trim();
			type = m.group(2).trim();
			switch(type) {
				case "modify":
					Map<String, String> kv = new HashMap<>();
					p = Pattern.compile("modify ([0-9a-zA-Z_-]+?) (char\\(\\d{1,100}\\)|float|double|int);");
					m = p.matcher(sql);
					if (m.find()) {
						kv.put("columnName", m.group(1).trim());
						kv.put("type", m.group(2).trim());
						conditions.put("modify", kv);
					} else {
						System.err.println("Error: Illegal Instruction!");
						throw new Exception();
					}
					break;
				case "add":
					Map<String, String> kv1 = new HashMap<>();
					p = Pattern.compile("add column ([0-9a-zA-Z_-]+?) (char\\(\\d{1,100}\\)|float|double|int);");
					m = p.matcher(sql);
					if (m.find()) {
						kv1.put("columnName", m.group(1).trim());
						kv1.put("type", m.group(2).trim());
						conditions.put("add", kv1);
					} else {
						System.err.println("Error: Illegal Instruction!");
						throw new Exception();
					}
					break;
				case "drop":
					Map<String, String> kv11 = new HashMap<>();
					p = Pattern.compile("drop column ([0-9a-zA-Z_-]+?);");
					m = p.matcher(sql);
					if (m.find()) {
						kv11.put("columnName", m.group(1).trim());
						conditions.put("drop", kv11);
					} else {
						System.err.println("Error: Illegal Instruction!");
						throw new Exception();
					}
					break;
				case "change":
					Map<String, String> kv111 = new HashMap<>();
					p = Pattern.compile("change ([0-9a-zA-Z_-]+?) ([0-9a-zA-Z_-]+?) (char\\(\\d{1,100}\\)|float|double|int);");
					m = p.matcher(sql);
					if (m.find()) {
						kv111.put("oldColumnName", m.group(1).trim());
						kv111.put("newColumnName", m.group(2).trim());
						kv111.put("type", m.group(3).trim());
						conditions.put("change", kv111);
					} else {
						System.err.println("Error: Illegal Instruction!");
						throw new Exception();
					}
					break;
				case "rename":
					Map<String, String> kv1111 = new HashMap<>();
					p = Pattern.compile("rename ([0-9a-zA-Z_-]+?);");
					m = p.matcher(sql);
					if (m.find()) {
						kv1111.put("newTableName", m.group(1).trim());
						conditions.put("rename", kv1111);
					} else {
						System.err.println("Error: Illegal Instruction!");
						throw new Exception();
					}
					break;
			}
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
	}

	public static void main(String... strings) throws Exception {
//		String a = "alter table (.+?) (modify|add|drop|change|rename) (.+);";
//
//		String b = "alter table emp modify ename varchar(20);";
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
//		AlterParser a = new AlterParser("alter table emp rename emp1;");
//		AlterParser b = new AlterParser("alter table emp modify ename char(10);");
		AlterParser c = new AlterParser("alter table emp add column age int;");
//		AlterParser d = new AlterParser("alter table emp drop column age;");
//		AlterParser e = new AlterParser("alter table emp change age age1 int;");
//		Pattern p = Pattern.compile("change ([0-9a-zA-Z_-]+?) ([0-9a-zA-Z_-]+?) (char\\(\\d{1,100}\\)|float|double|int);");
//		Matcher m = p.matcher("alter table emp change age age1 int;");
//		List<String> strs = new ArrayList<>();
//		while (m.find()) {
//			strs.add(m.group());
//		}
//		for (String str : strs) {
//			System.out.println(str);
//		}
	}
}
