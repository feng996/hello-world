package project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
/**
 * cur.dir: 
 * top.dir:D:\\eclipse\\eclipse-workspace\\sql_project3\\system
 * cur.database:
 * @author hasee
 *
 */
public class Information_Schema {
	private Set<String> databases;
	private boolean flag = true;
		
	public Information_Schema() throws IOException {
		databases = new HashSet<>();
		
		String path = System.getProperty("top.dir") + "\\my.information";
		Scanner in = new Scanner(Paths.get(path));
		
		while (in.hasNext()) {
			String name = in.nextLine();
			if (!name.trim().equals("")) {
				databases.add(name.trim());
			}
		}
		in.close();
	}
	
	public boolean IsExistDatabase(String databaseName)	{
		return databases.contains(databaseName);
	}
	
	public Database getDatabase(String databaseName) throws Exception {
		return new Database(databaseName);
	}
	
	public void addDatabase(String databaseName) throws IOException {
		databases.add(databaseName);
		String path = System.getProperty("top.dir") + "\\my.information";
		FileWriter fw = new FileWriter(path, true);
		fw.write("\r\n" + databaseName + "\r\n");
		fw.close();
		File dir = new File(System.getProperty("top.dir") + "\\" + databaseName);
		if(!dir.exists())
            dir.mkdir();
		dir = new File(System.getProperty("top.dir") + "\\" + databaseName + "\\" + databaseName + ".dictionary");
		if (!dir.exists()) {
			dir.createNewFile();
		}
	}
	
	public void dropDatabase(String databaseName) throws Exception {
		if (!databases.contains(databaseName)) {
			System.err.printf("Error: No (%s) database!\n ", databaseName);
			throw new Exception();
		}
		databases.remove(databaseName);
		String path = System.getProperty("top.dir") + "\\my.information";
		FileWriter fw = new FileWriter(path, false);
		for (String database: databases) {
			fw.write(database + "\r\n");
		}
		fw.close();
		path = System.getProperty("top.dir") + "\\" + databaseName;
		File file = new File(path);
		deleteAllFilesOfDir(file);
	}
	
	public void deleteAllFilesOfDir(File path) {  
	    if (!path.exists())  
	        return;  
	    if (path.isFile()) {  
	        path.delete();  
	        return;  
	    }  
	    File[] files = path.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        deleteAllFilesOfDir(files[i]);  
	    }  
	    path.delete();  
	}  
	
	public void show() {
		System.out.println("+----------+");
		System.out.println("| databases|");
		System.out.println("+----------+");
		
		for (String database: databases) {
			String base = database;
			if (base.length() >= 8) {
				base = base.substring(0, 5) + "...";
			}
			System.out.print("|");
			System.out.printf("%10s", base);
			System.out.print("|\n");
			System.out.println("+----------+");
		}
	}
}
