package handle;

import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsrDeleteHandle {
	private String usr;
	
	public UsrDeleteHandle(String sql) throws Exception {
		sql = getStandard(sql);
		
		Pattern p = Pattern.compile("^\\s*usrdelete \\s*(.+?)\\s*;$");
		Matcher m = p.matcher(sql);
		
		if (m.find()) {
			usr = m.group(1);
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
		if (usr.equals("root")) {
			System.err.println("Error: You don't have authority!");
			throw new Exception();
		}
		String path = System.getProperty("top.dir") + "\\my.usr";
		ArrayList<String> user = new ArrayList<String>();
		Scanner in = new Scanner(Paths.get(path));
		
		boolean flag = false;
		while (in.hasNext()) {
			String value = in.nextLine();
			if (value.trim().equals(" ")) {
				continue;
			}
			if (value.split(",")[0].trim().equals(usr)) {
				flag = true;
			} else {
				user.add(value);
			}
		}
		if (!flag) {
			System.err.printf("Error: No such (%s)!\n", usr);
			throw new Exception();
		}
		in.close();
		FileWriter fw = new FileWriter(path, false);
		for (String value: user) {
			fw.write(value + "\r\n");
		}
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
