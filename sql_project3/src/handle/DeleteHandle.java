package handle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import parser.DeleteParser;

public class DeleteHandle {
	private String tableName;
	private Map<String, ArrayList<String>> conditions;
	private DeleteParser parser;
	
	public DeleteHandle(String sql) throws Exception {
		parser = new DeleteParser(sql);
		tableName = parser.getTableName();
		conditions = parser.getConditions();
		Map<String, String> getTableName = new HashMap<>();
		getTableName.put(tableName, tableName);
		Table table = new Table(tableName, getTableName, null);
		
		table.deleteSelected(conditions);
		table.saveChange(tableName);
	}
	
	public static void main(String...strings) throws Exception {
		System.setProperty("cur.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system\\temp");
		DeleteHandle d = new DeleteHandle("delete from student where studentid=20163333;");
	}
}
