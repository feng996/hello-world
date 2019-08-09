package handle;

import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.DataColumn;
import project.DataTable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Table implements Cloneable {
	static ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");

	Set<String> tables; // 包含的表名
	private Map<String, DataColumn> tableColumns; // 初次建数据表时所有的列
	ArrayList<String> columnsSort; // 列的顺序 tableName.columnName
	Map<String, String> getTableName; // 别名
	private Map<String, Integer> columnsType; // 每个列对应的长度
	private ArrayList<String> columnsCurType; // 每个列对应的类型
	Map<String, Integer> typeLength;
	int lenAll = 0;

	ArrayList<Map<String, String>> tableDatas; // 每行包含的数据 tableName.columnName, string
	private ArrayList<Map<String, String>> parentDatas;
	private boolean flag;
	private Scanner in;

	public Table(String tableName, Map<String, String> getTableName, ArrayList<Map<String, String>> parentDatas)
			throws Exception {
		flag = true;
		this.parentDatas = parentDatas == null ? new ArrayList<>() : parentDatas;
		this.getTableName = getTableName;
		tableDatas = new ArrayList<>();
		typeLength = new HashMap<>();
		setLength();
		tables = new HashSet<>();
		tables.add(tableName);
		DataTable table = new DataTable(tableName);
		tableColumns = table.getColumns();
		columnsSort = table.getColumnsSort();
		String path = System.getProperty("cur.dir") + "\\" + tableName + ".data";
		in = new Scanner(Paths.get(path));
		String name = null;
		for (Map.Entry<String, String> kvp : getTableName.entrySet()) {
			if (kvp.getValue().equals(tableName)) {
				name = kvp.getKey();
			}
		}
		name = name + ".";
		for (int i = 0; i < columnsSort.size(); i++) {
			columnsSort.set(i, name + columnsSort.get(i));
		}
		while (in.hasNext()) {
			Map<String, String> kv = new HashMap<>();
			String[] datas = in.nextLine().split(",");
			for (int i = 0; i < datas.length; i++) {
				kv.put(columnsSort.get(i), datas[i].trim());
			}
			tableDatas.add(kv);
		}
		columnsType = new HashMap<>();
		getType();
		columnsCurType = new ArrayList<>();
		setType();
		
	}

	private void setLength() {
		typeLength.put("int", 8);
		typeLength.put("float", 8);
		typeLength.put("double", 8);
	}

	private void getType() {
		for (String column : columnsSort) {
			String type = tableColumns.get(column.split("\\.")[1]).getType();
			if (typeLength.containsKey(type)) {
				columnsType.put(column, typeLength.get(type));
			} else {
				Pattern p = Pattern.compile("char\\((\\d{1,100})\\)");
				Matcher m = p.matcher(type);
				if (m.find()) {
					columnsType.put(column, Integer.parseInt(m.group(1)));
				} else {
					columnsType.put(column, 5);
				}
			}
			int le = columnsType.get(column);
			le = (le/8 + 1) *8;
			lenAll += le;
		}
	}

	private void setType() {
		for (String column : columnsSort) {
			String type = tableColumns.get(column.split("\\.")[1]).getType();
			if (type.contains("char")) {
				type = "char";
			}
			columnsCurType.add(type);
		}
	}

	public void show() {
		if (tableDatas.size() == 0) {
			System.out.println("Empty Set");
			return;
		}
		String format = "+";
		for (int i = 0; i < lenAll-1; i++) {
			format += "-";
		}
		format += "+";
		System.out.println(format);
		System.out.print("|");
		for (String column : columnsSort) {
			String temp = column.split("\\.")[1].trim();
//			if (columnsType.get(column) <= temp.length()) {
//				temp = temp.substring(0, columnsType.get(column) - 4);
//				temp += "...";
//			}
			System.out.printf("%-" + columnsType.get(column) + "s\t", temp);
			System.out.print("|");
		}
		System.out.println();
		System.out.println(format);
		for (Map<String, String> kvp : tableDatas) {
			System.out.print("|");
			for (String column : columnsSort) {
				String temp = kvp.get(column).trim();
//				if (columnsType.get(column) <= temp.length()) {
//					temp = temp.substring(0, columnsType.get(column) - 4);
//					temp += "...";
//				}
				Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		        Matcher m = p.matcher(temp);
				System.out.printf("%-" + columnsType.get(column) + "s\t", temp);
				if (m.find()) {
					System.out.print("\t");
		        }
				System.out.print("|");
			}
			System.out.println();
		}
		System.out.println(format);
	}

	public void projection(ArrayList<String> columns) throws Exception {
//		for (String co: columns) {
//			System.out.println(co);
//		}
//		System.out.println("--------------");
//		for (String co: columnsSort) {
//			System.out.println(co);
//		}
		if (!(columns.size() == 1 && columns.get(0).equals("*"))) {
			for (int i = 0; i < columns.size(); i++) {
				if (columns.get(i).contains(".")) {
					if (!columnsSort.contains(columns.get(i))) {
						flag = false;
						System.err.println("Error: Without the column!");
						throw new Exception();
					}
				} else {
					boolean temp = false;
					for (Map.Entry<String, String> kv : getTableName.entrySet()) {
						if (new DataTable(kv.getValue()).hasColumn(columns.get(i))) {
							if (temp) {
								flag = false;
								System.err.println("Error: There are ambiguities!");
								throw new Exception();
							}
							columns.set(i, kv.getKey() + "." + columns.get(i));
							temp = true;
						}
					}
				}
			}
			columnsSort = columns;

			lenAll = 0;
			for (String column : columns) {
				int le = (columnsType.get(column)/8+1) * 8;
				lenAll += le;
			}

			ArrayList<Map<String, String>> newDatas = new ArrayList<>();
			for (Map<String, String> row : tableDatas) {
				Map<String, String> newRow = new HashMap<>();
				for (String column : columnsSort) {
					newRow.put(column, row.get(column));
				}
				newDatas.add(newRow);
			}
			tableDatas = newDatas;

			for (int i = 0; i < tableDatas.size(); i++) {
				Map<String, String> kv = tableDatas.get(i);
				for (int j = i + 1; j < tableDatas.size(); j++) {
					Map<String, String> kvp = tableDatas.get(j);
					boolean temp = false;
					for (String column : columnsSort) {
						if (!kv.get(column).equals(kvp.get(column))) {
							temp = true;
							break;
						}
					}
					if (!temp) {
						tableDatas.set(i, null);
					}
				}
			}

			tableDatas.removeIf(s -> s == null);
		}
	}

	public void multiply(Table other) {

		for (String table : other.tables) {
			tables.add(table);
		}

		for (Map.Entry<String, String> kvp : other.getTableName.entrySet()) {
			if (getTableName.containsKey(kvp.getKey())) {
				flag = false;
				return;
			}
			getTableName.put(kvp.getKey(), kvp.getValue());
		}

		for (Map.Entry<String, Integer> kvp : other.columnsType.entrySet()) {
			if (columnsType.containsKey(kvp.getKey())) {
				flag = false;
				return;
			}
			columnsType.put(kvp.getKey(), kvp.getValue());
		}
		lenAll += other.lenAll;

		ArrayList<Map<String, String>> newDatas = new ArrayList<>();
		for (Map<String, String> kvp : tableDatas) {
			for (Map<String, String> kv : other.tableDatas) {
				Map<String, String> newRows = new HashMap<>();
				for (String column : columnsSort) {
					newRows.put(column, kvp.get(column));
				}
				for (String column : other.columnsSort) {
					newRows.put(column, kv.get(column));
				}
				newDatas.add(newRows);
			}
		}
		tableDatas = newDatas;
		for (String column : other.columnsSort) {
			columnsSort.add(column);
		}
	}

	public void selectConditions(Map<String, ArrayList<String>> conditions) throws Exception {
		for (int i = 0; i < tableDatas.size(); i++) {
			boolean temp = false;
			for (String condition : conditions.get("or")) {
				if (isTrue(tableDatas.get(i), condition)) {
					temp = true;
					break;
				}
			}
			if (!temp) {
				temp = true;
				for (String condition : conditions.get("and")) {
					if (!isTrue(tableDatas.get(i), condition)) {
						temp = false;
						break;
					}
				}
			}
			if (!temp) {
				tableDatas.set(i, null);
			}
		}
		tableDatas.removeIf(s -> s == null);
	}
	
	public void deleteSelected(Map<String, ArrayList<String>> conditions) throws Exception {
		for (int i = 0; i < tableDatas.size(); i++) {
			boolean temp = false;
			for (String condition : conditions.get("or")) {
				if (isTrue(tableDatas.get(i), condition)) {
					temp = true;
					break;
				}
			}
			if (!temp) {
				temp = true;
				for (String condition : conditions.get("and")) {
					if (!isTrue(tableDatas.get(i), condition)) {
						temp = false;
						break;
					}
				}
			}
			if (temp) {
				tableDatas.set(i, null);
			}
		}
		tableDatas.removeIf(s -> s == null);
	}
	
	public void updateSelected(ArrayList<String[]> setConditions, Map<String, ArrayList<String>> conditions) throws Exception {
		for (int i = 0; i < tableDatas.size(); i++) {
			boolean temp = false;
			for (String condition : conditions.get("or")) {
				if (isTrue(tableDatas.get(i), condition)) {
					temp = true;
					break;
				}
			}
			if (!temp) {
				temp = true;
				for (String condition : conditions.get("and")) {
					if (!isTrue(tableDatas.get(i), condition)) {
						temp = false;
						break;
					}
				}
			}
			if (temp) {
				for (String[] lr : setConditions) {
					String left = lr[0];
					String right = lr[1];
					Pattern p = Pattern.compile("(\\+|-|\\*|/)");
					Matcher m = p.matcher(right);
					ArrayList<String> op = new ArrayList<String>();
					while (m.find()) {
						op.add(m.group(0));
					}
					String[] ele = right.split("\\+|-|\\*|/");

					for (Map.Entry<String, String> kv : tableDatas.get(i).entrySet()) {
						if (!left.contains(".")) {
							if (kv.getKey().contains(left)) {
								left = kv.getKey();
							}
						}
						for (int k = 0; k < ele.length; k++) {
							if (ele[k].contains(".")) {
								if (kv.getKey().equals(ele[k].trim())) {
									ele[k] = kv.getValue();
								}
							} else {
								if (kv.getKey().contains(ele[k].trim())) {
									ele[k] = kv.getValue();
								}
							}
						}
					}
					if (ele.length == 1) {
						right = ele[0];
					} else {
						right = ele[0];
						for (int k = 0; k < op.size(); k++) {
							right += op.get(k);
							right += ele[k + 1];
						}
						try {
							right = jse.eval(right).toString();
						} catch (Exception t) {
							t.printStackTrace();
						}
					}
					tableDatas.get(i).put(left, right);
				}
			}
		}
	}

	public boolean isTrue(Map<String, String> values, String condition) throws Exception {
		if (condition.trim().equals("true") || condition.trim().equals("false")) {
			return Boolean.parseBoolean(condition.trim());
		}
		condition = "   " + condition;
		String left = null;
		String mid = "";
		String right = null;
		if (!condition.contains("exists") && !condition.contains("in")) {
			Pattern p = Pattern.compile("(.+?)((>|<|=|!)+)(.+)");
			Matcher m = p.matcher(condition);
			if (m.find()) {
				left = m.group(1).trim();
				mid = m.group(2).trim();
				right = m.group(4).trim();
			} else {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
		} else {
			Pattern p = Pattern.compile("(.+?) (not\\s){0,1}(exists|in)\\s*\\((.+)\\)");
			Matcher m = p.matcher(condition);
			if (m.find()) {
				if (m.group(2) != null) {
					mid += "not";
				}
				left = m.group(1).trim();
				mid += m.group(3).trim();
				right = m.group(4).trim();
			} else {
				System.err.println("Error: Illegal Instruction!");
				throw new Exception();
			}
		}
		boolean hasDot = false;
		if (left.contains(".")) {
			hasDot = true;
		}
		boolean temp = false;
		for (Map.Entry<String, String> kv : values.entrySet()) {
			if (hasDot) {
				if (kv.getKey().equals(left)) {
					left = kv.getValue();
					temp = true;
					break;
				}
			} else {
				if (kv.getKey().contains(left)) {
					left = kv.getValue();
					temp = true;
					break;
				}
			}
		}
		if (!temp) {
			for (Map<String, String> kvp : parentDatas) {
				for (Map.Entry<String, String> kv : kvp.entrySet()) {
					if (hasDot) {
						if (kv.getKey().equals(left)) {
							left = kv.getValue();
							temp = true;
							break;
						}
					} else {
						if (kv.getKey().contains(left)) {
							left = kv.getValue();
							temp = true;
							break;
						}
					}
				}
				if (temp) {
					break;
				}
			}
		}
		hasDot = false;
		if (right.contains(".")) {
			hasDot = true;
		}
		temp = false;
		for (Map.Entry<String, String> kv : values.entrySet()) {
			if (hasDot) {
				if (kv.getKey().equals(right)) {
					right = kv.getValue();
					temp = true;
					break;
				}
			} else {
				if (kv.getKey().contains(right)) {
					right = kv.getValue();
					temp = true;
					break;
				}
			}
		}
		if (!temp) {
			for (Map<String, String> kvp : parentDatas) {
				for (Map.Entry<String, String> kv : kvp.entrySet()) {
					if (hasDot) {
						if (kv.getKey().equals(right)) {
							right = kv.getValue();
							temp = true;
							break;
						}
					} else {
						if (kv.getKey().contains(right)) {
							right = kv.getValue();
							temp = true;
							break;
						}
					}
				}
				if (temp) {
					break;
				}
			}
		}

		Map<String, String> then = new HashMap<>();
		for (Map.Entry<String, String> kv : values.entrySet()) {
			then.put(kv.getKey(), kv.getValue());
		}
		ArrayList<Map<String, String>> newAr = new ArrayList<>();
		newAr.add(then);

		for (Map<String, String> kv : parentDatas) {
			Map<String, String> next = new HashMap<>();
			for (Map.Entry<String, String> kvp : kv.entrySet()) {
				next.put(kvp.getKey(), kvp.getValue());
			}
			newAr.add(next);
		}
		if (left.contains("'")) {
			left = left.substring(1, left.length() - 1);
		}
		if (right.contains("'") && !right.contains("select")) {
			right = right.substring(1, right.length() - 1);
		}
		try {
			switch (mid) {
			case "=":
				return left.equals(right);
			case "!=":
				return !left.equals(right);
			case ">":
				return Double.parseDouble(left) > Double.parseDouble(right);
			case ">=":
				return Double.parseDouble(left) >= Double.parseDouble(right);
			case "<":
				return Double.parseDouble(left) < Double.parseDouble(right);
			case "<=":
				return Double.parseDouble(left) <= Double.parseDouble(right);
			case "in":
				return new SelectHandle(right + ";", newAr).handle().isIn(left);
			case "notin":
				return !new SelectHandle(right + ";", newAr).handle().isIn(left);
			case "exists":
				return new SelectHandle(right + ";", newAr).handle().isExists();
			case "notexists":
				return !new SelectHandle(right + ";", newAr).handle().isExists();
			default:
				flag = false;
				return false;
			}
		} catch (Exception e) {
			System.err.println("Error: Illegal Conditions!");
			throw new Exception();
		}
	}

	public boolean isIn(String value) {
		if (columnsSort.size() != 1) {
			System.out.println("Error!");
			/**
			 * 错误
			 */
			return false;
		}
		boolean temp = false;
		for (Map<String, String> values : tableDatas) {
			if (values.get(columnsSort.get(0)).equals(value)) {
				temp = true;
				break;
			}
		}
		return temp;
	}

	public boolean isExists() {
		return tableDatas.size() != 0;
	}
	
	public void saveChange(String name) throws IOException {
		String path = System.getProperty("cur.dir") + "\\" + name + ".data";
		
		FileWriter fw = new FileWriter(path, false);
		for (Map<String, String> kv: tableDatas) {
			String value = "";
			for (String column: columnsSort) {
				String temp = kv.get(column);
				if (temp.contains("'")) {
					temp = temp.substring(1, temp.length()-1);
				}
				value += temp + ",";
			}
			value = value.substring(0, value.length()-1);
			value += "\r\n";
			fw.write(value);
		}
		fw.close();
	}

	public Object clone() {
		Table obj = null;
		try {
			obj = (Table) super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println(e.toString());
		}
		return obj;
	}

	public static void main(String... strings) throws Exception {
		Map<String, String> st = new HashMap<>();
		st.put("student", "student");
		Map<String, String> te = new HashMap<>();
		te.put("teacher", "teacher");
		Table student = new Table("student", st, null);
		Table teacher = new Table("teacher", te, null);
		student.show();
		teacher.show();

		student.multiply(teacher);
		student.show();
		ArrayList<String> temp = new ArrayList<>();
		temp.add("studentid");
		temp.add("teacherid");
		student.projection(temp);
		student.show();
	}
}
