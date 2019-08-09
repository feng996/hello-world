package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateParser {
	private String type;
	private String name;
	private Map<String, Map<String, String>> columns;
	private ArrayList<String> columnsSort;
	private ArrayList<String> primary;
	private boolean flag;
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, Map<String, String>> getColumns() {
		return columns;
	}
	
	public ArrayList<String> getPrimary() {
		return primary;
	}
	
	public ArrayList<String> getColumnsSort() {
		return columnsSort;
	}
	
	public CreateParser(String sql) throws Exception {
		flag = true;
		columns = new HashMap<>();
		primary = new ArrayList<>();
		columnsSort = new ArrayList<>();
		sql = getStandard(sql);
		sqlParser(sql);
//		System.out.println(flag);
//		System.out.println(type);
//		System.out.println(name);
//		for(Map.Entry<String, Map<String, String>> kvp: columns.entrySet()) {
//			System.out.println(kvp.getKey());
//			for(Map.Entry<String, String> kv: kvp.getValue().entrySet()) {
//				System.out.println(kv.getKey() + ", " + kv.getValue());
//			}
//		}
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
		Pattern p = Pattern.compile("(table|database) (.+?)(\\s|\\(|;)");
		Matcher m = p.matcher(sql);
		if (m.find()) {
			type = m.group(1).trim();
			name = m.group(2).trim();
			if (type.equals("database")) {
				p = Pattern.compile("^\\s*create database " + name + ";$");
				m = p.matcher(sql);
				if (!m.find()) {
					System.err.println("Error: Illegal Instruction!");
					throw new Exception();
				}
				return;
			}
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
		
		String strings;
		p = Pattern.compile("(" + name + "\\s*\\()(.+)\\)");
		m = p.matcher(sql);
		if(m.find()) {
			strings = m.group(2).trim();
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
		String[] conditions = strings.split(",");
		for (String condition: conditions) {
			condition = condition.trim();
			String[] values = condition.split(" ");
			Map<String, String> kvp = new HashMap<>();
			if (values.length == 2) {
				if (values[0].equals("primary")) {
					values[1] = strings.substring(strings.indexOf("key"), strings.length());
					p = Pattern.compile("^(key\\()(.+?)\\)$");
					m = p.matcher(values[1]);
					if (m.find()) {
						for(String temp: m.group(2).split(",")) {
							primary.add(temp.trim());
						}
					} else {
						System.err.println("Error: Illegal Instruction!");
						throw new Exception();
					}
					break;
				} else {
					p = Pattern.compile("char\\(\\d{1,100}\\)|float|double|int");
					m = p.matcher(values[1]);
					if (m.find()) {
						kvp.put("name", values[0].trim());
						kvp.put("type", values[1].trim());
						kvp.put("canNull", "true");
						kvp.put("hasIndex", "false");
						kvp.put("isPrime", "false");
					} else {
						System.err.println("Error: Illegal Instruction!");
						throw new Exception();
					}
				}
			} else if (values.length == 4) {
				if (!values[2].equals("not") || !values[3].equals("null")) {
					System.err.println("Error: Illegal Instruction!");
					throw new Exception();
				}
				p = Pattern.compile("char\\(\\d{1,100}\\)|float|double|int");
				m = p.matcher(values[1]);
				if (m.find()) {
					kvp.put("name", values[0].trim());
					kvp.put("type", values[1].trim());
					kvp.put("canNull", "false");
					kvp.put("hasIndex", "false");
					kvp.put("isPrime", "false");
				} else {
					System.err.println("Error: Illegal Instruction!");
					throw new Exception();
				}
			} else {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
			if (columns.containsKey(values[0])) {
				System.err.println("Error: The column aready exists!");
				throw new Exception();
			}
			columnsSort.add(values[0]);
			columns.put(values[0], kvp);
		}
		
		for (String prime: primary) {
			if (!columns.containsKey(prime)) {
				System.err.printf("Error: can't find %s column\n!", prime);
				throw new Exception();
			}
			Map<String, String> newMap = new HashMap<>();
			for(Map.Entry<String, String> kv: columns.get(prime).entrySet()) {
				if (kv.getKey().equals("isPrime")) {
					newMap.put("isPrime", "true");
				} else {
					newMap.put(kv.getKey(), kv.getValue());
				}
			}
			columns.put(prime, newMap);
		}
	}
	
	public static void main(String...strings) throws Exception {
//		String a = "^[a-zA-Z][a-zA-Z0-9_-]*$";
//		
//		String b = "create table a(a char(10) not null, b int not null, c int, primary key(a))";
//		
//		String c = "(table|database) (.+?)(\\s|\\()";
//		String d = "(a\\s*\\()(.+)\\)";
//		String e = "a char(10) not null, b int not null, c int, primary key(a)";
//		String[] es = e.split(",");
//		String temp = "a char(10) not null";
//		String d = "(.+?) (.+?)(\\s*(not null){0,1})";
//		String e = "int";
//		String f = "char\\(\\d{1,100}\\)|float|double|int";
//		List<String> strs = new ArrayList<>();
//		Pattern p = Pattern.compile(f);
//		Matcher m = p.matcher(e);
//		
//		while (m.find()) {
//			strs.add(m.group());
//		}
//		for (String str: strs) {
//			System.out.println(str);
//		}
//		CreateParser p = new CreateParser("create table a (a char(10) not null, b int not null, c int, primary key(a));");
		CreateParser p = new CreateParser("table student_class (sid int not null, sname char(10), cid int not null, cname char(10), primary key(sid, cid));");
//		CreateParser p = new CreateParser("create database user1;");
//		Pattern p = Pattern.compile("(table|database) (.+?)(\\s|\\(|;)");
//		Matcher m = p.matcher("create database user1;");
//		List<String> strs = new ArrayList<>();
//		while (m.find()) {
//			strs.add(m.group(2));
//		}
//		for (String str: strs) {
//			System.out.println(str);
//		}
	}
	
}
