package handle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.DataTable;

public class CreateIndexHandle {
	private String name;
	private String tableName;
	private String columnName;
	
	public CreateIndexHandle(String sql) throws Exception {
		sql = getStandard(sql);
		parser(sql);
		new DataTable(tableName).addIndex(name, columnName);
//		System.out.println(name);
//		System.out.println(tableName);
//		System.out.println(columnName);
	}
	
	private String getStandard(String sql) {
		sql = sql.trim();
		sql = sql.toLowerCase();
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.substring(0, sql.lastIndexOf(";")+1);
		sql = "" + sql;
		return sql;
	}
	
	private void parser(String sql) throws Exception {
		Pattern p = Pattern.compile("\\s*(idxcreate) (.+?) (.+?) (.+);");
		Matcher m = p.matcher(sql);
		
		if (m.find()) {
			name = m.group(2).trim();
			tableName = m.group(3).trim();
			columnName = m.group(4).trim();
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
	}
	
	public static void main(String...strings) throws Exception {
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		System.setProperty("cur.database", "temp");
		String a = "idxcreate employee_ssn employee ssn;";
		new CreateIndexHandle(a);
	}
}
