package handle;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.Data_Dictionary;
import project.Information_Schema;

public class DropHandle {
	private String type;
	private String name;
	private boolean flag = true;
	
	public DropHandle(String sql) throws Exception {
		sql = getStandard(sql);
		parser(sql);
		handle();
	}
	
	private void parser(String sql) throws Exception {
		Pattern p = Pattern.compile("^drop (database|table) (.+);$");
		Matcher m = p.matcher(sql);
		
		if (m.find()) {
			type = m.group(1);
			name = m.group(2);
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
	}
	
	public void handle() throws Exception {
		switch(type) {
		case "database":
			databaseHandle();
			break;
		case "table":
			tableHandle();
			break;
		}
	}
	
	public void databaseHandle() throws Exception {
		new Information_Schema().dropDatabase(name);
	}
	
	public void tableHandle() throws Exception {
		new Data_Dictionary().dropTable(name);
	}
	
	public String getStandard(String sql) {
		sql = sql.trim();
		sql = sql.toLowerCase();
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.substring(0, sql.lastIndexOf(";")+1);
		sql = "" + sql;
		return sql;
	}
	
	public static void main(String...strings) throws Exception {
		System.setProperty("top.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system");
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		System.setProperty("cur.database", "temp");
		DropHandle d = new DropHandle("drop database temp;");
	}
}
