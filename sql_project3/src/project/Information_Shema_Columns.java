package project;

public class Information_Shema_Columns {
	private String databaseName;
	private String tableName;
	private String columnName;
	private String columnType;
	
	public Information_Shema_Columns(String databaseName, 
			String tableName, String columnName, String columnType) {
		this.databaseName = databaseName;
		this.tableName = tableName;
		this.columnName = columnName;
		this.columnType = columnType;
	}
	
	public Information_Shema_Columns(String...strings) {
		this.databaseName = strings[0];
		this.tableName = strings[1];
		this.columnName = strings[2];
		this.columnType = strings[3];
	}
	
	public String getDatabaseName() {
		return databaseName;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public String getColumnType() {
		return databaseName;
	}
}
