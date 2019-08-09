package project;

import java.io.IOException;

public class Database {
	private String name;
	public Data_Dictionary dataDictionary;

	public Database(String name) throws Exception {
		try {
			dataDictionary = new Data_Dictionary();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new Exception();
		}
	}
}
