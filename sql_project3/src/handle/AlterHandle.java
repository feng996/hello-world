package handle;

import java.io.IOException;
import java.util.Map;

import parser.AlterParser;
import project.DataTable;
import project.Data_Dictionary;

public class AlterHandle {
	private String tableName;
	private String type;
	private Map<String, String> conditions;
	private AlterParser parser;
	
	public AlterHandle(String sql) throws Exception {
		parser = new AlterParser(sql);
		
		tableName = parser.getTableName();
		type = parser.getType();
		conditions = parser.getConditions();
//		for(Map.Entry<String, String> kv: conditions.entrySet()) {
//			System.out.println(kv.getKey() + ", " + kv.getValue());
//		}
		handle();
	}
	
	public void handle() throws Exception {
		switch(type) {
		case "modify":
			modifyHandle();
			break;
		case "add":
			addHandle();
			break;
		case "drop":
			dropHandle();
			break;
		case "change":
			changeHandle();
			break;
		case "rename":
			renameHandle();
			break;
		}
	}
	
	public void modifyHandle() throws Exception {
		DataTable table = new DataTable(tableName);
		table.modifyType(conditions.get("columnName"), conditions.get("type"));
	}
	
	public void addHandle() throws Exception {
		DataTable table = new DataTable(tableName);
		table.addColumn(conditions.get("columnName"), conditions.get("type"));
	}
	
	public void dropHandle() throws Exception {
		DataTable table = new DataTable(tableName);
		table.dropColumn(conditions.get("columnName"));
	}
	
	public void changeHandle() throws Exception {
		DataTable table = new DataTable(tableName);
		table.changeColumn(conditions.get("oldColumnName"), conditions.get("newColumnName"), conditions.get("type"));
	}
	
	public void renameHandle() throws Exception {
		Data_Dictionary dic = new Data_Dictionary();
		dic.rename(tableName, conditions.get("newTableName"));
	}
	
	public static void main(String...strings) throws Exception {
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		System.setProperty("cur.database", "temp");
//		AlterHandle a = new AlterHandle("alter table student modify studentid char(10);");
//		AlterHandle b = new AlterHandle("alter table student add column sss int;");
//		AlterHandle c = new AlterHandle("alter table student drop column sss;");
//		AlterHandle d = new AlterHandle("alter table student change studentnum studentnum int;");
		AlterHandle e = new AlterHandle("alter table student1 rename student;");
	}
}
