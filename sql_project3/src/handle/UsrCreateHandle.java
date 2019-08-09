package handle;

import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsrCreateHandle {
	private String usr;
	private String psw;
	
	public UsrCreateHandle(String sql) throws Exception {
		sql = getStandard(sql);
		
		Pattern p = Pattern.compile("^\\s*usrcreate (.+?) (.+?)\\s*;$");
		Matcher m = p.matcher(sql);
		
		if (m.find()) {
			usr = m.group(1);
			psw = m.group(2);
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
		
		String path = System.getProperty("top.dir") + "\\my.usr";
		
		FileWriter fw = new FileWriter(path, true);
		fw.write(usr + "," + psw + "\r\n");
		fw.close();
	}
	
	private String getStandard(String sql) {
		sql = sql.trim();
		sql = sql.toLowerCase();
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.substring(0, sql.lastIndexOf(";") + 1);
		sql = "" + sql;
		return sql;
	}
}
