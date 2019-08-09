package project;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import handle.AlterHandle;
import handle.CreateHandle;
import handle.CreateIndexHandle;
import handle.DeleteHandle;
import handle.DescHandle;
import handle.DropHandle;
import handle.GrantHandle;
import handle.IndexDropHandle;
import handle.InsertHandle;
import handle.RevokeHandle;
import handle.SelectHandle;
import handle.ShowHandle;
import handle.UpdateHandle;
import handle.UseHandle;
import handle.UsrCreateHandle;
import handle.UsrDeleteHandle;

public class DataSystem {
	private String user;
	
	public String getUsr() {
		return user;
	}
	
	public DataSystem(String name, String password) throws Exception {
		String path = System.getProperty("top.dir") + "\\my.usr";
		
		Scanner in = new Scanner(Paths.get(path));
		boolean flag = false;
		while (in.hasNext()) {
			String info = in.nextLine();
			if (info.split(",")[0].equals(name)) {
				flag = true;
				if (!info.split(",")[1].trim().equals(password)) {
					System.err.println("Error: Password Mistake!");
					throw new Exception();
				}
				user = name;
			}
		}
		if (!flag) {
			System.err.printf("Error: No (%s) user!\n", name);
			throw new Exception();
		}
	}
	
	public static void main(String... strings) {
		System.setProperty("top.dir", "D:\\eclipse\\eclipse-workspace\\sql_project3\\system");
		DataSystem sys;
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.print("Please enter a user name: ");
			String user = in.nextLine();
			System.out.print("Please enter password: ");
			String psw = in.nextLine();
			try {
				sys = new DataSystem(user, psw);
			} catch (Exception e1) {
				try {
	                Thread.sleep(1);
	            } catch (InterruptedException e2) {
	                e2.printStackTrace();
	            }
				continue;
			}
			break;
		}
		
		System.setProperty("cur.usr", sys.getUsr());
		
		try {
			Information_Schema information = new Information_Schema();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// System.setProperty("cur.database", "false");
		
		while (true) {
			System.out.print("sql> ");
			String statement = in.nextLine();
			while (statement.lastIndexOf(";") != statement.length() - 1) {
				statement = statement + " " + in.nextLine();
			}
			statement = statement.trim().toLowerCase();
			String[] values = statement.split(" ");

			String operate = values[0];
			boolean flag = true;
			try {
				switch (operate.trim()) {
				case "insert":
					new InsertHandle(statement);
					break;
				case "delete":
					new DeleteHandle(statement);
					break;
				case "update":
					new UpdateHandle(statement);
					break;
				case "select":
					new SelectHandle(statement, null).handle().show();
					break;
				case "show":
					new ShowHandle(statement);
					break;
				case "alter":
					new AlterHandle(statement);
					break;
				case "drop":
					new DropHandle(statement);
					break;
				case "create":
					new CreateHandle(statement);
					break;
				case "use":
					new UseHandle(statement);
					break;
				case "desc":
					new DescHandle(statement);
					break;
				case "grant":
					new GrantHandle(statement);
					break;
				case "revoke":
					new RevokeHandle(statement);
					break;
				case "usrcreate":
					new UsrCreateHandle(statement);
					break;
				case "usrdelete":
					new UsrDeleteHandle(statement);
					break;
				case "idxcreate":
					new CreateIndexHandle(statement);
					break;
				case "idxdrop":
					new IndexDropHandle(statement);
					break;
				case "quit;":
					System.exit(0);
					break;
				default:
					System.err.println("Error: Illegal Instruction!\n");
					throw new Exception();
				}
			} catch (Exception e) {
				try {
	                Thread.sleep(1);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace();
	            }
//				continue;
			}
		}
	}

}
