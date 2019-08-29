package demo1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableDemo {
	public static void main(String...strings) throws Exception {
		User user = new User();
		user.setName("cxk");
		user.setAge(18);
		System.out.println(user);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("template"));
		
		
		oos.writeObject(user);
		oos.close();
		
		File file = new File("template");
		ObjectInputStream ios = new ObjectInputStream(new FileInputStream(file));
		
		User newUser = (User) ios.readObject();
		System.out.println(newUser);
	}
}
