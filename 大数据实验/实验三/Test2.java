package hadoopTest;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 该类利用MapReduce实现统计词频并输出出现频率在前十的单词
 * 该实例包含两次MapReduce任务
 * @author 肖凌峰
 */

public class Test2 {

	/**
	 * 阶段一的Mapper，用来实现将文档内容进行划分
	 *
	 * 输入文本举例："This is a demo to test the program , if you run the demo , you will get the result"
	 * 输出结果为：<This, 1> <is, 1> <a, 1> <demo, 1>...	
	 *
	 * @param Object 			Mapper类的第一个输入参数类型，Mapper默认的键类型是IntWritable类型，用来做行标号
	 * @param Text 				Mapper类的第二个输入参数类型，用来获取输入的文本内容
	 * @param Text 				Mapper类的第一个输出参数类型，输出时键的参数类型
	 * @param IntWritable 		Mapper类的第二个输出参数类型，输出时值的参数类型
	 * @author 肖凌峰
	 */
	
	public static class MyFirstMapper extends Mapper<Object, Text, Text, IntWritable> {
		private static final IntWritable one = new IntWritable(1);	//值为1的IntWritable类型常量，输出的值对象
		private Text word = new Text();								//输出的键对象
		
		/**
		 * 该方法为重写父类Mapper的map方法
		 * 方法用于从指定路径下的文档中，将文档内容做单词分割，并作为键和上述值对象一起输出，作为Reduce函数的输入。
		 * @author 肖凌峰
		 * @DateTime 2018-12-30T11:45:23+0800
		 * @param    key                      Mapper的键输入参数，类型为Object，Mapper的输入key对象默认为IntWritable，即为行标号
		 * @param    value                    Mapper的值输入参数，类型为Text，用于获取指定路径下的文档内容
		 * @param    context                  用来传递数据以及其他运行状态信息，类型为Mapper<Object, Text, Text, IntWritable>.Context
		 * @throws   IOException              文件读写出现的异常
		 * @throws   InterruptedException     提供可中断响应方法或线程，对于处于异常状态下的线程或方法，抛出InterruptedException并提前返回
		 */
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());	//利用默认方式对文本进行词分割
			
			while (itr.hasMoreElements()) {			//遍历单词
				this.word.set(itr.nextToken());		//将单词字符串设置为键
				context.write(word, one);			//写入数据<word, count>
			}
		}
	}

	/**
	 * 阶段一的Reducer，用来实现将上个Mapper得到的结果进行合并统计
	 *
	 * 输入举例：<This, 1> <is, 1> <a, 1> <demo, 1>...	
	 * 输出结果为：<This, 1> <is, 1> <a, 1> <demo, 2>...	
	 * 
	 * @author 肖凌峰
	 */

	public static class MyFirstReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable res = new IntWritable();		//定义装结果值的容器
		
		/**
		 * 该方法为重写父类Reducer的reduce方法
		 * 方法用来对每个单词出现的频率进行统计，并写入文件中，用作下一阶段的Mapper输入
		 * @author 肖凌峰
		 * @DateTime 2018-12-30T16:45:01+0800
		 * @param    key                      	Reducer的键输入参数，类型为上个Mapper的键输出类型Text，内容为上述输出键值对中的键内容
		 * @param    values{@code IntWritable}	Reducer的值输入参数，类型为上个Mapper的值输出类型IntWritable的集合Iterable，内容为上述输出键值对中的值内容集合
		 * @param    context                  	用来传递数据以及其他运行状态信息，类型为Reducer<Text, IntWritable, Text, IntWritable>.Context
		 * @throws   IOException              	文件读写出现的异常
		 * @throws   InterruptedException     	提供可中断响应方法或线程，对于处于异常状态下的线程或方法，抛出InterruptedException并提前返回
		 */

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;		//保存每个词对应的词频和
			
			for (IntWritable val: values) {		//遍历值统计集合
				sum += val.get();
			}
			
			res.set(sum);		//设置结果值容器值
			context.write(key, res);		//写入输入<word, count>
		}
	}

	/**
	 * 阶段二的Mapper，用来实现将上阶段MapReduce任务获得的结果进行键值对反转
	 * 因为我们需要统计出频率出现前十的单词，又因为Map函数后的shuffle是按键进行排序的，所以无法对值进行统计排序
	 * 因此阶段二的任务就是将键值对反转后将结果按词频进行排序，最后再反转将前十的词频单词写入文件中
	 *
	 * 输入举例：<This, 1> \n <is, 1> \n <a, 1> \n <demo, 2>...
	 * 输出举例：<1, This> <1, is> <1, a> <2, demo>...
	 *
	 * @param Object 			Mapper类的第一个输入参数类型，Mapper默认的键类型是IntWritable类型，用来做行标号
	 * @param Text 				Mapper类的第二个输入参数类型，用来获取输入的文本内容
	 * @param IntWritable 		Mapper类的第一个输出参数类型，输出时键的参数类型
	 * @param Text 				Mapper类的第二个输出参数类型，输出时值的参数类型
	 * @author 肖凌峰
	 */
	
	public static class MySecondMapper extends Mapper<Object, Text, IntWritable, Text> {
		private Text word = new Text();							//定义存放输出值内容的容器
		private IntWritable count = new IntWritable();			//定义存放输出键内容的容器

		/**
		 * 该方法为重写父类Mapper的map方法
		 * 方法用于将上阶段MapReduce结果进行键值对反转输出
		 * @author 肖凌峰
		 * @DateTime 2018-12-30T17:16:22+0800
		 * @param    key                      输入键参数，类型为Object，此处为行标号
		 * @param    values                   输入值参数，类型为Text，此处是指定路径下的文本内容
		 * @param    context                  用来传递数据以及其他运行状态信息，类型为Mapper<Object, Text, IntWritable, Text>.Context
		 * @throws   IOException              文件读写出现的异常
		 * @throws   InterruptedException     提供可中断响应方法或线程，对于处于异常状态下的线程或方法，抛出InterruptedException并提前返回
		 */

		public void map(Object key, Text values, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(values.toString(), "\n");		//将文本内容按行划分获得类似<word, count>的迭代器
			
			while (itr.hasMoreElements()) {			//遍历迭代器
				StringTokenizer itr2 = new StringTokenizer(itr.nextToken());	//将<word, count> 划分为word何count
				word.set(itr2.nextToken());			//将word值写入输出值容器
				count.set(Integer.parseInt(itr2.nextToken()));		//将count值写入输出键容器
				context.write(count, word);			//写入文件<count, word>
			}
			
		}
	}

	/**
	 * 阶段二的Reducer，用来实现将阶段二的Mapper后的键值对反转并输出前十个
	 * 
	 * 输入举例：<2, <demo, the, ...>> <1, <This, is, a, ...>> ...
	 * 输出举例：<demo, 2> <the, 2> <..., 2> <This, 1>...（共10个键值对，对于其他的不写入文件）
	 *
	 * @param IntWritable 		Reducer类的第一个输入参数类型，上个Mapper的键输出内容
	 * @param Text 				Reducer类的第二个输入参数类型，上个Mapper的值输出内容
	 * @param Text 				Reducer类的第一个输出参数类型，输出时键的参数类型
	 * @param IntWritable 		Reducer类的第二个输出参数类型，输出时值的参数类型
	 * @author 肖凌峰
	 */

	public static class MySecondReducer extends Reducer<IntWritable, Text, Text, IntWritable> {
		private int count = 10;		//实验要求输出词频率前十的单词，这里count=10
		
		/**
		 * 该方法为重写父类Reducer的reduce方法
		 * 方法输出前10个单词和出现的频率
		 * @author 肖凌峰
		 * @DateTime 2018-12-30T18:49:57+0800
		 * @param    key                      	Reducer的键输入参数，类型为上个Mapper的键输出类型IntWritable，内容为上述输出键值对中的键内容
		 * @param    values{@code Text}			Reducer的值输入参数，类型为上个Mapper的值输出类型Text的集合Iterable，内容为上述输出键值对中的值内容集合
		 * @param    context                  	用来传递数据以及其他运行状态信息，类型为Reducer<IntWritable, Text, Text, IntWritable>.Context
		 * @throws   IOException              	文件读写出现的异常
		 * @throws   InterruptedException     	提供可中断响应方法或线程，对于处于异常状态下的线程或方法，抛出InterruptedException并提前返回
		 */

		public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			/*
			如果count != 0的话就将<key, value>反转成<value, key>写入文件
			因为上一步Mapper是按词频进行排序的所以<key, value>实例举例为<2, demo>
			输出时我们需要进行反转
			这里values类似<2, <demo, the, ...>>
			二阶段开始时也有示例
			 */
			for (Text value: values) {
				if (count == 0) {
					return;
				}
				context.write(value, key);
				count--;
			}
		}
	}
	
	/**
	 * 该类自定义了一个比较器，因为在MapReduce中map任务做完后的shuffle中会有排序过程，但是由于是升序
	 * 而我们需要统计频率出现高的前十位，所以我们不能采取升序，我们需要降序并从头输出十个。
	 * 这里相当于重定义比较器，让shuffle排序时按照降序的方式进行排序
	 */

	public static class IntWritableDecreasingComparator extends IntWritable.Comparator {
		/**
		 * 这个函数具体如何运作还没有看源代码，只知道IntWritable.Comparator是继承的WritableComparator，
		 * compare函数是RawComparator接口的实现，功能就是实现一个比较器，在shuffle排序中实现。
		 * @author 肖凌峰
		 * @DateTime 2018-12-30T19:18:26+0800
		 * @param    b1                       第一个字节数组
		 * @param    s1                       b1对象的起始索引
		 * @param    l1                       b1对象的长度
		 * @param    b2                       第二个字节数组
		 * @param    s2                       b2对象的起始索引
		 * @param    l2                       b2对象的长度
		 * @return                            返回一个带符号的整数
		 */
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			return -super.compare(b1, s1, l1, b2, s2, l2);		//这里调用父类的compare函数，只是将结果取反即可降序排序
		}
	}
	
	public static void main(String...strings) throws IOException, ClassNotFoundException, InterruptedException {
//		Configuration conf1 = new Configuration();
//		String[] otherArgs = new String[] {"test2/input", "test2/output1"， "test2/output2"};
//		Job job1 = Job.getInstance(conf1, "WordCount1");
//		Path outputPath = new Path(otherArgs[1]);
//        outputPath.getFileSystem(conf1).delete(outputPath, true);
//        
//        job1.setJarByClass(Test2.class);
//        job1.setMapperClass(Test2.MyFirstMapper.class);
//        job1.setCombinerClass(Test2.MyFirstReducer.class);
//        job1.setReducerClass(Test2.MyFirstReducer.class);
//        job1.setOutputKeyClass(Text.class);
//        job1.setOutputValueClass(IntWritable.class);
//        
//        FileInputFormat.addInputPath(job1, new Path(otherArgs[0]));
//        FileOutputFormat.setOutputPath(job1, new Path(otherArgs[1]));
//        job1.waitForCompletion(true);
//        
//        Configuration conf2 = new Configuration();
//        otherArgs = new String[] {"test2/output1", "test2/output2"};
//        Job job2 = Job.getInstance(conf2, "WordCount2");
//        outputPath = new Path(otherArgs[2]);
//        outputPath.getFileSystem(conf2).delete(outputPath, true);
//        
//        job2.setJarByClass(Test2.class);
//        job2.setMapperClass(Test2.MySecondMapper.class);
//        job2.setReducerClass(Test2.MySecondReducer.class);
//        job2.setMapOutputKeyClass(IntWritable.class);
//        job2.setMapOutputValueClass(Text.class);
//        job2.setOutputKeyClass(Text.class);
//        job2.setOutputValueClass(IntWritable.class);
//        job2.setSortComparatorClass(IntWritableDecreasingComparator.class);
//        
//        
//        FileInputFormat.addInputPath(job2, new Path(otherArgs[1]));
//        FileOutputFormat.setOutputPath(job2, new Path(otherArgs[2]));
//        job2.waitForCompletion(true);
//        
		/**
		 * JobConf是MapReduce任务的配置对象
		 * 也是描述MapReduce任务在Mapreduce框架中如何执行的主要途径
		 * 框架将如实的以该对象包含的信息来执行MapReduce任务
		 * 单例JobConf的结果只能在JobTask中可以获取，而在其他类中没法获取
		 * 由于资源隔离，map任务和reduce任务无法共享task中JobConf之前的set设置
		 * Hadoop环境更推荐使用Configuration进行设置和获取相关配置
		 * 当然使用JobConf是因为看书上有使用这个类的事例，就拿来用了
		 * 用Configuration是一样的，在这个实验和下个实验环境下，不需要获取传递其中的设置参数，所以不会出现问题
		 * 以后写其他东西可能会在参数传递方面有一些区别
		 * 以我现在只是听别人说有区别，还没有碰到问题，等以后有待发现吧
		 * 上面注释的内容就是用Configuration实现的
		 * 上述纯属闲话，只是在写注释的时候偶然看到有大佬讲解这个区别，所以看了下，记录下来。
		 * 
		 */
		JobConf conf = new JobConf();		//用户描述到Hadoop框架执行的map-reduce作业的主要接口
		
		String[] otherArgs = new String[] {"test2/input", "test2/output1", "test2/output2"};	//定义输入输出路径，这里每个输出为一个阶段的MapReduce任务的结果记录，方便查看
		Job job1 = Job.getInstance(conf, "WordCount1");		//申请一个作业job1
		Path outputPath = new Path(otherArgs[1]);			
        outputPath.getFileSystem(conf).delete(outputPath, true);	//每次运行作业都要将上次生成的输出目录删除，不然会报错
       
        job1.setJarByClass(Test2.class);		//设置运行环境
        job1.setMapperClass(Test2.MyFirstMapper.class);		//设置Map运行环境
        job1.setCombinerClass(Test2.MyFirstReducer.class);	//设置shuffle合并的运行环境
        job1.setReducerClass(Test2.MyFirstReducer.class);	//设置Reduce运行环境
        job1.setOutputKeyClass(Text.class);		//设置输出键类型，这里Map和Reduce输出键类型相同，只设置一个
        job1.setOutputValueClass(IntWritable.class);		//设置输出值类型，这里Map和Reduce输出值类型相同，只设置一个
        
        ControlledJob ctrljob1 = new ControlledJob(conf);	//申请一个控制容器ctrljob1
        ctrljob1.setJob(job1);								//将job1装入刚申请的容器中
        FileInputFormat.addInputPath(job1, new Path(otherArgs[0]));		//设置job1输入路径
        FileOutputFormat.setOutputPath(job1, new Path(otherArgs[1]));	//设置job1输出路径
        
        Job job2 = Job.getInstance(conf, "WordCount2");		//申请一个作业job2
        outputPath = new Path(otherArgs[2]);
        outputPath.getFileSystem(conf).delete(outputPath, true);	//每次运行作业都要将上次生成的输出目录删除，不然会报错
        		
        job2.setJarByClass(Test2.class);		//设置运行环境
        job2.setMapperClass(Test2.MySecondMapper.class);	//设置Map运行环境
        job2.setReducerClass(Test2.MySecondReducer.class);	//设置Reduce运行环境
        job2.setMapOutputKeyClass(IntWritable.class);		//设置Mapper任务输出键类型
        job2.setMapOutputValueClass(Text.class);			//设置Mapper任务输出值类型
        job2.setOutputKeyClass(Text.class);					//设置Reducer任务输出键类型
        job2.setOutputValueClass(IntWritable.class);		//设置Reducer任务输出值类型，因为该阶段
        job2.setSortComparatorClass(IntWritableDecreasingComparator.class);		//因为需要让它降序排序，这里设置比较器运行环境
        
        ControlledJob ctrljob2 = new ControlledJob(conf);	//申请一个控制容器ctrljob2
        ctrljob2.setJob(job2);								//将job2装入刚申请的容器中
        ctrljob2.addDependingJob(ctrljob1);					//设置job2和job1的依赖关系，
        													//因为这里是指job2只有在job1完成后进行，因为job2的输入是job1的输出
        													//依赖关系需要通过控制容器实现，所以这里实际添加的控制容器直接的关系
        FileInputFormat.addInputPath(job2, new Path(otherArgs[1]));		//设置job2输入路径
        FileOutputFormat.setOutputPath(job2, new Path(otherArgs[2]));	//设置job2输出路径
        
        JobControl jobctrl = new JobControl("job");			//申请作业控制器
        jobctrl.addJob(ctrljob1);							//添加job1的容器
        jobctrl.addJob(ctrljob2);							//添加job2的容器
        
        Thread  t=new Thread(jobctrl);   					//给控制器创建进程
        t.start(); 											//作业开始
        while (true) {
			if (jobctrl.allFinished()) {					//只有当作业控制器中的所有作业都完成了，进程才停止
				jobctrl.stop();
				break;
			}
		}
	}
}
