package handle;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.Data_Dictionary;
import project.Information_Schema;

public class ShowHandle {
	
	private String type;
	private boolean flag = true;
	
	public void parser(String sql) {
		Pattern p = Pattern.compile("^show (databases|tables)\\s*;$");
		Matcher m = p.matcher(sql);
		if (m.find()) {
			type = m.group(1);
		} else {
			flag = false;
			return;
		}
	}
	
	public ShowHandle(String sql) throws Exception {
		sql = getStandard(sql);
		parser(sql);
		switch(type.trim()) {
		case "databases":
			databaseShow();
			break;
		case "tables":
			if (System.getProperty("cur.database") == null) {
				System.err.println("Error: No database selected!");
				throw new Exception();
			}
			tablesShow();
			break;
		}
	}
	
	public void databaseShow() throws IOException {
		new Information_Schema().show();
	}
	
	public void tablesShow() throws IOException {
		new Data_Dictionary().show();
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
//		System.out.println(System.getProperty("cur.database"));
		System.setProperty("top.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system");
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		System.setProperty("cur.database", "temp");
		ShowHandle s = new ShowHandle("show tables;");
//		Pattern p = Pattern.compile("^show (databases|tables);$");
//		Matcher m = p.matcher("show databases;");
//		
//		if (m.find()) {
//			System.out.println(m.group(1));
//		}
	}
}
