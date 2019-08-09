package handle;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.Information_Schema;

public class UseHandle {
	private String databaseName;
	private boolean flag = true;
	
	public UseHandle(String sql) throws Exception {
		parser(sql);
		if (new Information_Schema().IsExistDatabase(databaseName)) {
			System.setProperty("cur.database", databaseName);
			System.setProperty("cur.dir", System.getProperty("top.dir") + "\\" + databaseName);
		} else {
			System.err.printf("Error: No (%s) database!\n", databaseName);
			throw new Exception();
		}
	}
	
	public void parser(String sql) throws Exception {
		sql = getStandard(sql);
		Pattern p = Pattern.compile("^use (.+);$");
		Matcher m = p.matcher(sql);
		
		if (m.find()) {
			databaseName = m.group(1).trim();
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
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
}
