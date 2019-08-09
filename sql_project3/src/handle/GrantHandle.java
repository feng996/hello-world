package handle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import project.UserOperate;

public class GrantHandle {
	private String usr;
	private String type;
	private String tableName;
	public GrantHandle(String sql) throws Exception {
		sql = getStandard(sql);
		Pattern p = Pattern.compile("^\\s*grant (.+?) (.+?) (.+?)\\s*;$");
		Matcher m = p.matcher(sql);
		
		if(m.find()) {
			usr = m.group(1);
			type = m.group(2);
			tableName = m.group(3);
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
		new UserOperate(System.getProperty("cur.usr")).addGrant(usr, type, tableName);
//		System.out.println(usr);
//		System.out.println(type);
//		System.out.println(tableName);
	}
	
	private String getStandard(String sql) {
		sql = sql.trim();
		sql = sql.toLowerCase();
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.substring(0, sql.lastIndexOf(";") + 1);
		sql = "" + sql;
		return sql;
	}
	
	public static void main(String...strings) throws Exception {
		new GrantHandle("   grant  root  select  student   ;");
	}
}
