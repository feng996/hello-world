package project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Data_Dictionary {
	// private Map<String, DataTable> tables;
	private Set<String> tables;
	private boolean flag = true;

	public Data_Dictionary() throws IOException {
		tables = new HashSet<String>();
		// tables = new HashMap<String, DataTable>();
		String path = System.getProperty("cur.dir") + "\\" + System.getProperty("cur.database") + ".dictionary";
		Scanner in = new Scanner(Paths.get(path));

		while (in.hasNext()) {
			String name = in.nextLine();
			tables.add(name);
		}
	}

	public boolean hasTable(String name) {
		return tables.contains(name);
	}

	public DataTable getTable(String name) throws Exception {
		if (hasTable(name)) {
			return new DataTable(name);
		} else {
			System.err.printf("Error: No (%s) table!\n ", name);
			throw new Exception();
		}

	}

	public void rename(String oldName, String newName) throws Exception {
		if (oldName.equals(newName)) {
			return;
		}
		if (tables.contains(oldName)) {
			tables.remove(oldName);
			tables.add(newName);
			File oldfile = new File(System.getProperty("cur.dir") + "\\" + oldName + ".table");
			File newfile = new File(System.getProperty("cur.dir") + "\\" + newName + ".table");
			if (newfile.exists())
				System.out.println(newName + " has existsed！");
			else {
				oldfile.renameTo(newfile);
			}
			oldfile = new File(System.getProperty("cur.dir") + "\\" + oldName + ".data");
			newfile = new File(System.getProperty("cur.dir") + "\\" + newName + ".data");
			if (newfile.exists())
				System.out.println(newName + " has existsed！");
			else {
				oldfile.renameTo(newfile);
			}
			String path = System.getProperty("cur.dir") + "\\" + System.getProperty("cur.database") + ".dictionary";
			FileWriter fw = new FileWriter(path, false);
			for (String table: tables) {
				fw.write(table + "\r\n");
			}
			fw.close();
		} else {
			System.err.printf("Error: No (%s) Form!\n", oldName);
			throw new Exception();
		}
	}

	public void addTable(String name, ArrayList<String> columnsSort, Map<String, Map<String, String>> columns)
			throws IOException {

		String path = System.getProperty("cur.dir") + "\\" + System.getProperty("cur.database") + ".dictionary";
		FileWriter fw = new FileWriter(path, true);
		fw.write(name + "\r\n");
		fw.close();
		File file = new File(System.getProperty("cur.dir") + "\\" + name + ".table");
		if (!file.exists())
			file.createNewFile();
		file = new File(System.getProperty("cur.dir") + "\\" + name + ".data");
		if (!file.exists())
			file.createNewFile();

		Map<String, DataColumn> newColumns = new HashMap<>();

		for (String column : columnsSort) {
			DataColumn datacolumn = new DataColumn(columns.get(column).get("name"), columns.get(column).get("type"),
					Boolean.parseBoolean(columns.get(column).get("canNull")),
					Boolean.parseBoolean(columns.get(column).get("isPrime")),
					Boolean.parseBoolean(columns.get(column).get("hasIndex")));
			newColumns.put(column, datacolumn);
		}
		DataTable dataTable = new DataTable(name, newColumns, new HashMap<String, IndexTable>(), columnsSort);
		dataTable.reSave();
	}
	
	public void dropTable(String name) throws Exception {
		if (!tables.contains(name)) {
			System.err.printf("Error: No (%s) Form!\n", name);
			throw new Exception();
		}
		tables.remove(name);
		String path = System.getProperty("cur.dir") + "\\" + System.getProperty("cur.database") + ".dictionary";
		FileWriter fw = new FileWriter(path, false);
		for (String table: tables) {
			fw.write(table + "\r\n");
		}
		fw.close();
		
		path = System.getProperty("cur.dir") + "\\" + name + ".table";
		File file = new File(path);
		file.delete();
		path = System.getProperty("cur.dir") + "\\" + name + ".data";
		file = new File(path);
		file.delete();
	}
	
	public void show() {
		System.out.println("+----------+");
		System.out.println("|    tables|");
		System.out.println("+----------+");
		
		for (String table: tables) {
			String base = table;
			if (base.length() >= 10) {
				base = base.substring(0, 7) + "...";
			}
			System.out.print("|");
			System.out.printf("%10s", base);
			System.out.print("|\n");
			System.out.println("+----------+");
		}
	}

}
