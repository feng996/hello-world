package project;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UserOperate {
	private Map<String, ArrayList<String>> grant;
	private String userName;

	public UserOperate(String name) throws Exception {
		
		String path = System.getProperty("top.dir") + "\\my.usr";
		boolean flag = false;
		Scanner in = new Scanner(Paths.get(path));
		while (in.hasNext()) {
			String info = in.nextLine();
			if (info.trim().equals("")) {
				continue;
			}
			if (info.split(",")[0].equals(name)) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			System.err.printf("Error: No such (%s)!\n", name);
			throw new Exception();
		}
		
		userName = name;
		path = System.getProperty("top.dir") + "\\user.data";

		grant = new HashMap<>();
		grant.put("select", new ArrayList<>());
		grant.put("delete", new ArrayList<>());
		grant.put("insert", new ArrayList<>());
		grant.put("update", new ArrayList<>());
		
		in = new Scanner(Paths.get(path));
		while (in.hasNext()) {
			String info = in.nextLine();
			if (info.trim().equals("")) {
				continue;
			}
			String[] values = info.split(",");
			if (values[0].trim().equals(name)) {
				grant.get(values[1].trim()).add(values[2]);
			} else {
				continue;
			}
		}
		in.close();
	}
	
	public boolean hasThisGrant(String ope, String table) {
		return !grant.get(ope).contains(table);
	}
	
	public void addGrant(String user, String ope, String table) throws Exception {
		if (user.equals("root")) {
			System.err.println("Error: You don't have authority!");
			throw new Exception();
		}
		if (new UserOperate(user).hasThisGrant(ope, table)) {
			System.err.printf("Error: (%s) already have access!\n", user);
			throw new Exception();
		}
		String path = System.getProperty("top.dir") + "\\user.data";
		Scanner in = new Scanner(Paths.get(path));
		ArrayList<String> data = new ArrayList<>();
		while (in.hasNext()) {
			String info = in.nextLine();
			if (info.trim().equals("")) {
				continue;
			}
			String[] values = info.split(",");
			if (values[0].trim().equals(user) && values[1].trim().equals(ope) && 
					values[2].trim().equals(table)) {
				continue;
			} else {
				data.add(info);
			}
		}
		in.close();
		FileWriter fw = new FileWriter(path, false);
		for (String value: data) {
			fw.write(value + "\r\n");
		}
		fw.close();
		
	}
	
	public void save(String name, String ope, String table) throws IOException {
		String path = System.getProperty("top.dir") + "\\user.data";
		FileWriter fw = new FileWriter(path, true);
		String temp = name + "," + ope + "," + table + "\r\n";
		fw.write(temp);
		fw.close();
	}
	
	public void revoke(String name, String ope, String table) throws Exception {
		if (name.equals("root") || !hasThisGrant(ope, table)) {
			System.err.println("Error: You don't have authority!");
			throw new Exception();
		}
		if (!new UserOperate(name).hasThisGrant(ope, table)) {
			System.err.printf("Error: (%s) don't have this authority!\n", name);
			throw new Exception();
		} else {
			save(name, ope, table);
		}
	}
	
	public Map<String, ArrayList<String>> getGrant() {
		return grant;
	}
	
	public static void main(String...strings) throws Exception {
		System.setProperty("top.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system");
		UserOperate u = new UserOperate("root");
//		u.revoke("test", "select", "student");
		u.addGrant("test", "select", "student");
	}
}
