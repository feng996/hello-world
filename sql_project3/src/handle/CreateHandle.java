package handle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import parser.CreateParser;
import project.Data_Dictionary;
import project.Database;
import project.Information_Schema;

public class CreateHandle {
	private String type;
	private String name;
	private ArrayList<String> columnsSort;
	private ArrayList<String> primary;
	private Map<String, Map<String, String>> columns;
	private CreateParser parser;
	private boolean flag;
	
	public CreateHandle(String sql) throws Exception {
		flag = true;
		parser = new CreateParser(sql);
		
		type = parser.getType();
		name = parser.getName();
		columnsSort = parser.getColumnsSort();
		primary = parser.getPrimary();
		columns = parser.getColumns();
//		for(Map.Entry<String, Map<String, String>> kvp: columns.entrySet()) {
//			System.out.println(kvp.getKey());
//			for(Map.Entry<String, String> kv: kvp.getValue().entrySet()) {
//				System.out.println(kv.getKey() + ", " + kv.getValue());
//			}
//		}
		
		if (type.equals("database")) {
			createDatabase();
		} else if(type.equals("table")){
			createTable();
		} else {
			System.err.println("Error: Illegal Instruction!");
			throw new Exception();
		}
	}
	
	public void createDatabase() throws Exception {
		Information_Schema information = new Information_Schema();
		if (!information.IsExistDatabase(name.trim())) {
			information.addDatabase(name.trim());
		} else {
			System.err.println("Error: The database already exists!");
			throw new Exception();
		}
	}
	
	public void createTable() throws Exception {
		Data_Dictionary dataDictionary = new Data_Dictionary();
		if (!dataDictionary.hasTable(name)) {
			Data_Dictionary dictionary = new Data_Dictionary();
			dictionary.addTable(name, columnsSort, columns);
		} else {
			System.err.println("Error: The table already exists!");
			throw new Exception();
		}
	}
	
	public static void main(String...strings) throws Exception {
		System.setProperty("top.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system");
		System.setProperty("cur.database", "temp");
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
//		CreateHandle c = new CreateHandle("create table student_class (sid int not null, sname char(10), cid int not null, cname char(10), primary key(sid, cid));");
		CreateHandle c = new CreateHandle("create database test;");
	}
}
