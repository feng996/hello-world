package handle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.DataTable;

public class IndexDropHandle {
	private String name;
	private String tableName;
	
	public IndexDropHandle(String sql) throws Exception {
		sql = getStandard(sql);
		parser(sql);
		new DataTable(tableName).dropIndex(name);
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
		Pattern p = Pattern.compile("^\\s*(idxdrop) (.+?) (.+?)\\s*;$");
		Matcher m = p.matcher(sql);
		
		if (m.find()) {
			name = m.group(2).trim();
			tableName = m.group(3).trim();
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
	}
}
