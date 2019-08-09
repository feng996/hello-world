package hadoopTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * 本类用来实现HDFS的读写
 * 1.通过向HDFS中写入两个分别包含十个数的文件
 * 2.并通过从HDFS中读出这两个文件并将排序结果写回HDFS
 * @author 肖凌峰
 */

public class Test1 {

	/**
 	* 该方法实现随机生成10个数并写到指定的HDFS路径下
 	* @param  paths{@code String} 用于给定需要存储的HDFS路径数组
 	*/

	public static void writeTohdfs(String...paths) {
		try {
			Configuration conf = new Configuration();		//  该对象封装了客户端和服务器的配置
			conf.set("fs.defaultFS", "hdfs://localhost:9000");		//指定服务器
			conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");	//设置HDFS文件操作类
			FileSystem fs = FileSystem.get(conf);							//	获得默认文件系统
			for (String path: paths) {						//对于每个路径写入数据并写到文件系统中
				FSDataOutputStream os = fs.create(new Path(path));	//在文件系统下创建路径指定文件
				String str1 = "";
				for (int i = 0; i < 10; i++) {				//生成十个随机整数
					str1 += (int)(Math.random()*100)+1 + " ";	
				}
				byte[] str = str1.getBytes();				//转换成字节流
				os.write(str, 0, str.length);				//将字节流写入文件系统
				os.close();									//关闭文件流
			}
			fs.close();										//关闭文件系统
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
 	* 该方法实现通过读取指定路径下的所有文件并将内容排序写入到给定的文件系统路径
 	* @param  paths{@code String} 用于给定需要读取的HDFS路径数组
 	*/

	public static void sortTohdfs(String...paths) {
		try {
			Configuration conf = new Configuration();
			conf.set("fs.defaultFS", "hdfs://localhost:9000");
			conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
			FileSystem fs = FileSystem.get(conf);
			ArrayList<Integer> numbers = new ArrayList<>();		//用于保存从文件中读取的数据
			for (String path: paths) {
				Path file = new Path(path);
				FSDataInputStream getIt = fs.open(file);		//获取文件输入流
				BufferedReader d = new BufferedReader(new InputStreamReader(getIt));	//给文件输入流加管道方便读写
				for (String value: d.readLine().split(" ")) {	//根据写入时的格式读写每一个数据
					numbers.add(Integer.parseInt(value.trim()));	//将数据写入数组集合中
				}
				getIt.close();		//关闭输入流
			}
			Collections.sort(numbers);	//给集合排序
			
			byte[] str = numbers.toString().getBytes();		//将集合内容转换成字节流
			FSDataOutputStream os = fs.create(new Path("test1/project1output"));	//获取文件输出流
			os.write(str, 0, str.length);		//写入文件系统
			os.close();		//关闭输出流
			fs.close();		//关闭文件系统
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String...strings) {
		writeTohdfs("test1/input1", "test1/input2");		//生成数据并写入文件系统
		sortTohdfs("test1/input1", "test1/input2");			//读取上面生成的数据，排序后写入文件系统

		/**
		 * 也可以指定路径
		 * writeTohdfs(strings);
		 * sortTohdfs(strings);
		 */
	}
}
