package handle;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.DataTable;

public class DescHandle {
	private String tableName;
	private boolean flag = true;
	
	public DescHandle(String sql) throws Exception {
		if (System.getProperty("cur.database") == null) {
			System.err.println("Error: No database selected!");
			throw new Exception();
		}
		parser(sql);
		new DataTable(tableName).show();
	}
	
	public void parser(String sql) throws Exception {
		Pattern p = Pattern.compile("^desc (.+);$");
		Matcher m = p.matcher(sql);
		if (m.find()) {
			tableName = m.group(1);
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
	}
	
	public static void main(String...strings) throws Exception {
		System.setProperty("top.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system");
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		System.setProperty("cur.database", "temp");
		DescHandle d = new DescHandle("desc student;");
	}
}
