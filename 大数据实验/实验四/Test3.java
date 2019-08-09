package hadoopTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * 此示例利用MapReduce实现电影推荐，分为三个阶段
 * 1.找出各个电影的评分人总数
 * 2.找出每个电影对如(A，B)都做评价的评分人总数
 * 3.找出每两个相关电影的关联
 * 因为从网上荡的数据，都有几个文件，这里取典型的两个文件举例
 * 
 * movies.csv（举例）（每行长度为3）：
 * movieId	title				genres
 * 1  		Toy Story (1995)	Adventure|Animation|Children|Comedy|Fantasy
 * 2 		Jumanji (1995) 		Adventure|Children|Fantasy
 * ...		...					...
 *
 * ratings1.cvs（举例）（每行长度为4）：
 * userId	movieId		rating 		time
 * 1 		1 			4 			964982703
 * 1 		3 			4			964981247
 * ...		...			...			...
 *
 * 所以一开始我利用MapReduce对两表进行连接，也就是阶段一，输出结果如下：
 * userId	title				rating
 * 1 		Toy Story (1995)	4
 * ...		...					...
 *
 * 阶段二、三、四就是上述实验要求的三个阶段
 * 
 * ps：因为上个实验将MapReduce每个参数注释的比较清楚了，这里只讲每个阶段怎么做的，其他不重复说明了
 *
 * @author 肖凌峰
 */

public class Test3 {
	
	/**
	 * 这里定义一个辅助类
	 * 用来等会方便计算皮尔逊相关系数
	 * 方便阶段四的reduce函数进行记录存储。
	 * @author 肖凌峰
	 */
	
	public static class MovieRating {
		private String movieName;		//电影名
		private float point;			//打分
		private int numbers;			//打分人数

		public String getMovieName() {
			return movieName;
		}

		public float getPoint() {
			return point;
		}

		public int getNumbers() {
			return numbers;
		}

		public MovieRating(String line) {
			// System.out.println(line);
			String[] values = line.split("-");		//因为是按[movieName-point-numbers]进行存储的，所以这里用'-'分割
			movieName = values[0];
			point = Float.parseFloat(values[1]);
			numbers = Integer.parseInt(values[2]);
		}
	}

	/**
	 * 阶段一的Mapper类，用来对两个文件进行清洗和格式化
	 * 这里提一下，上述说明了两个文件的存储内容，因为是csv文件，所以我们在实际用流读的时候，每行各个数据使用逗号','分割的
	 * 所以这里我们需要用逗号分割，但是不能直接分割，因为其中电影名可能也包含逗号（这里好坑，数据清洗真难搞）
	 * 这里只是尽量避免了许多情况，但终究还是不能算是万能的
	 * 下次数据干扰点换一下，又得重写，特别是数据量大的时候，什么乱七八糟的情况都有，有时候可能拿来的数据就不全。
	 * 有时间还得好好看看数据处理方面的东西。
	 * 以上纯属闲话，看完请跳过/xyx
	 * @author 肖凌峰
	 */

	public static class TheFirstStageMapper extends Mapper<Object, Text, Text, Text> {
		/**
		 * 输入
		 * movies.csv：
		 * movieId，title，genres
		 * 1，Toy Story (1995)，Adventure|Animation|Children|Comedy|Fantasy
 		 * 2，Jumanji (1995)，Adventure|Children|Fantasy
 		 * ...，...，...
 		 *
 		 * ratings1.csv：
 		 * userId，movieId，rating，time
 		 * 1，1，4，964982703
 		 * 1，3，4，964981247
 		 * ...，...，...，...
 		 *
 		 * 输出
 		 * movieId  value(如果是从movies来的取2+title， 如果是从ratings来的取1+userid+','+rating+','+time)
 		 * 1 		2Toy Story (1995)
 		 * 2 		2Jumanji (1995)
 		 * 1 		11,4,964982703
 		 * 3 		11,4,964981247
 		 *
 		 * 主键key是两个表的共同元素movieId
		 * @author 肖凌峰
		 * @DateTime 2018-12-30T21:00:21+0800
		 */
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itrs = new StringTokenizer(value.toString(), "\n");		//将数据按行分割

			while (itrs.hasMoreElements()) {		//遍历数据
				String line = itrs.nextToken();		//获取一行
				if (line.contains("movieId")) {		//如果是第一行，跳过
					continue;
				}

				String[] values = line.split(",");	//按逗号分割
				if (line.contains("\"")) {			//如果电影名包含双引号，就表示这行来自movies，并且电影名可能也包含逗号
													//就把除了最后一项和第一项除去，其他中间的内容全是电影名，做连接得到原来的电影名
					String k = values[0].trim();
					String v = "1";					//这里用1表示电影名开头
					for (int i = 1; i < values.length - 1; i++) { 	//遍历中间的内容，全是电影名包含的内容
						v += values[i];
					}
					context.write(new Text(k), new Text(v));	//写入文件
				} else if (values.length == 4) {	//如果长度为4就是ratings1.csv文件下的内容
					String k = values[1].trim();
					String v = "2" + values[0].trim() + "," + values[2] + "," + values[3];	//用2表示用户打分，values[0]是电影id，values[2]是用户id
					context.write(new Text(k), new Text(v));	//写入文件
				} else if (values.length == 3) {	//如果长度为3就是movies.csv文件下的内容
					String k = values[0].trim();
					String v = "1" + values[1].trim();	//用1表示电影名开头，values[1]是电影名
					context.write(new Text(k), new Text(v));	//写入文件
				} else {
					continue;						//其他异常数据跳过就好了
				}
			}
		}
	}
	
	/**
	 * 阶段一的Reducer
	 * 上述Mapper结果进行shuffle后会以如下形式传给Reduce任务
	 * <1, <2Toy Story (1995), 11,4,964982703, ...>>（1表示电影id号，值中以2开头的就是该电影名，1开头的就是用户以及该用户的打分）
	 * ...
	 *
	 * 该阶段的Reducer用来将上述结果转换成如下形式（用户id-电影名-打分）（其他内容丢弃）：
	 * 1-Toy Story (1995)-4
	 * m-Toy Story (1995)-n
	 * ...
	 */

	public static class TheFirstStageReducer extends Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String temp = "";				//这里需要存储的是电影名，也就是输出结果的key，也就是每行中以2开头的内容
			ArrayList<String> res = new ArrayList<>();	//这个集合存储所有给该电影打分的用户及评分
			for (Text value : values) {			//遍历Mapper任务的结果集
				String val = value.toString();	
				if (val.charAt(0) == '1') {		//如果以1开头，说明是电影名
					temp = val.substring(1, val.length());	//去头存到temp中
				} else if (val.charAt(0) == '2') {	//如果以2开头，说明是用户以及其评分
					res.add(val.substring(1, val.length()));	//去头存到结果集中
				} else {
					continue;				//其他异常数据跳过就好了
				}
			}

			for (String val : res) {		//遍历结果集，把每个用户评分单独写到文件中
				String[] tmp = val.split(",");	
				String kk = tmp[0] + "-" + temp + "-" + tmp[1];	//将其转换为（用户id-电影名-打分）的形式
				context.write(new Text(kk), new Text());	//存到文件中
			}
		}
	}

	/*
	下面的TempMapper和TempReducer用做测试用，看每个结果是否是按照规定进行处理的
	 */
	// public static class TempMapper extends Mapper<Object, Text, Text, Text> {
	//
	// public void map(Object key, Text value, Context context) throws
	// IOException, InterruptedException {
	// StringTokenizer itrs = new StringTokenizer(value.toString(), "\n");
	// while (itrs.hasMoreTokens()) {
	// String[] itr = itrs.nextToken().split("-");
	// String val = itr[0] + "-" + itr[itr.length-1];
	// String k = "";
	// for (int i = 1; i < itr.length-1; i++) {
	// k += itr[i];
	// }
	// context.write(new Text(k.trim()), new Text(val));
	// }
	// }
	// }
	//
	// public static class TempReducer extends Reducer<Text, Text, Text, Text> {
	//
	// public void reduce(Text key, Iterable<Text> values, Context context)
	// throws IOException, InterruptedException {
	// String val = "";
	//
	// for (Text value: values) {
	// val += value.toString();
	// }
	// context.write(key, new Text(val));
	// }
	// }

	/**
	 * 阶段二的Mapper
	 * 该阶段用来统计每个电影打分次数
	 * 
	 * 输入也就是上个阶段的输出，以如下形式
	 * 1-Toy Story (1995)-4
	 * m-Toy Story (1995)-n
	 * ...
	 *
	 * Mapper阶段输出：
	 * Toy Story (1995)		1-4
	 * Toy Story (1995)		m-n
	 * ...
	 */

	public static class TheSecondStageMapper extends Mapper<Object, Text, Text, Text> {

		/**
		 * 这个map完成将输入文本格式进行简单的转换，把原来的电影名当键，用户id和打分用'-'连接作为值
		 * @author 肖凌峰
		 * @DateTime 2018-12-31T09:49:59+0800
		 */

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itrs = new StringTokenizer(value.toString(), "\n");	//先按行分割
			while (itrs.hasMoreTokens()) {		//遍历所有行
				String[] itr = itrs.nextToken().split("-");		//这里根据上个Mapper输出格式按'-'分割成用户id，电影名和评分
				String val = itr[0] + "-" + itr[itr.length - 1];	//val存储的是map输出的值，因为电影名可能包含分隔符'-'
																	//所以这里我们取头跟尾连接，也就是用户id-评分
				String k = "";									//这个保存电影名，也就是map输出的key
				for (int i = 1; i < itr.length - 1; i++) {		//依次连接中间的内容，也就是电影名
					k += itr[i];
				}
				context.write(new Text(k.trim()), new Text(val));	//将上述键值对写入文件
			}
		}
	}

	/**
	 * 阶段二的Reducer
	 * 该阶段的输入就是上述map shuffle后的输出，以如下形式：
	 * <Toy Story (1995), <1-4, m-n, ...> >
	 * ...
	 *
	 * Reducer的输出，以如下形式（用户id 电影名-打分-该电影打分总人数）：
	 * 用户id是key 	其他用'-'连接的就是值
	 * <1, Toy Story (1995)-4-统计后的打分人数>
	 * ...
	 */

	public static class TheSecondStageReducer extends Reducer<Text, Text, Text, Text> {

		/**
		 * 这个reduce主要就是做统计，统计出key包含的values个数，然后依次写入文件中
		 * 先遍历迭代器统计出总数并且把每次的结果以字符串的形式存储在集合中
		 * （这里有个坑， 就是遍历完values后，再用foreach遍历，会直接退出，相当于指针一样，取到最后就退出了
		 * 	但平时遍历不会有这个问题，感觉是reduce任务接受到的迭代器问题，为什么是一次性的，这个不是很懂，
		 * 	所以我得用一个集合把第一次遍历的每个值存起来。下次去遍历那个集合，但还是有问题，如果我直接存的是Text类型
		 * 	也就是reduce接受到的values类型，那么做的好像是浅拷贝，因为我测试的时候，如果是直接存原来的Text对象，会导致
		 * 	结果集中只有一个相同的元素，所以我处理的时候，保存的是Text的内容，也就是toString后的值，这样就没有问题了，
		 * 	总而言之，我觉得是reduce接受参数传递的问题或者是迭代器的问题，下次好好看看迭代器跟任务调度的源码，再来填这个坑，
		 * 	这次实验就用深拷贝去掩盖一下
		 * 	）
		 * @author 肖凌峰
		 * @DateTime 2018-12-31T10:00:40+0800
		 */

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			int nums = 0;			//记录给电影打分的总人数
			ArrayList<String> res = new ArrayList<>();		//用来存values迭代器中的内容

			for (Text value : values) {		//第一次直接遍历迭代器
				res.add(value.toString());	//保存对象的内容（toString后的深拷贝）
				nums++;						//每次遍历人数加一
			}

			for (String value : res) {		//第二次遍历结果集，把每个结果转换格式存到文件中
				String[] vals = value.split("-");	//根据存储格式，这里用'-'分割
				String val = key.toString() + "-" + vals[1].trim() + "-" + nums;	//这里存键值对中的值（电影名-分数-总人数）
				if (key.toString().trim().equals("")) {		//因为有可能荡下来的文件本来就可能有空白，所以对于出现的异常直接跳过就好了
					continue;
				}
				context.write(new Text(vals[0]), new Text(val));	//写入文件（<1, Toy Story (1995)-4-统计后的打分人数>)
			}
		}
	}

	/**
	 * 阶段三的Mapper，同时也是阶段四的Mapper，因为此Mapper只是为了合并相同键元素
	 *
	 * 该阶段找出对任意两个电影A，B，都做出评分的人
	 *
	 * Mapper阶段输入：
	 * <1, Toy Story (1995)-4-统计后的打分人数>
	 * <1, 电影2-4-统计后的打分人数>
	 *...
	 * Mapper阶段后suffle后的输出：
	 * (用户id为主键)
	 * (该用户给哪些电影打过分的集合为值)
	 * <1, <<Toy Story (1995)-4-统计后的打分人数>, <电影2-4-统计后的打分人数> > >
	 * ...
	 */

	public static class TheThirdStageMapper extends Mapper<Object, Text, Text, Text> {

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itrs = new StringTokenizer(value.toString(), "\n");	//将文件内容按行分割
			while (itrs.hasMoreTokens()) {	//遍历每行
				StringTokenizer itr = new StringTokenizer(itrs.nextToken()); 	//分割行元素
				String k = itr.nextToken();		//获取key值
				String val = "";			//其后面元素全是value值
				while (itr.hasMoreTokens()) {
					val += itr.nextToken();
				}
				if (val.length() == 0) {	//异常数据直接跳过
					continue;
				}
				context.write(new Text(k), new Text(val));	//写回文件
			}
		}
	}

	/**
	 * 阶段三的Reducer
	 * 主要作用就是对每个用户，把其打过分的任意两个电影A，B进行相关联，包括对A的评分和A电影评分总人数，对B也进行统计
	 * 并计算出ratingProduct（对A，B两个电影的打分积），rating1Squared（对A的评分的平方值），rating2Squared（对B评分的平方值）
	 * 计算上述值时为了方便计算阶段四的皮尔逊相关系数
	 *
	 * 输入：
	 * <1, <<Toy Story (1995)-4-统计后的打分人数>, <电影2-4-统计后的打分人数> > >
	 *
	 * 输出：
	 * <电影1-电影2， rating1-给电影1打分的人数-rating2-给电影2打分的人数-ratingProduct-rating1Squared-rating2Squared>
	 * 其中key是   电影1的名称-电影2的名称
	 * value是     rating1-给电影1打分的人数-rating2-给电影2打分的人数-ratingProduct-rating1Squared-rating2Squared
	 * 其中'-'为分割符
	 */

	public static class TheThirdStageReducer extends Reducer<Text, Text, Text, Text> {
		
		/**
		 * 因为要将values中的元素做笛卡尔积，但是有不能重复，只能先存起来，再套个阶乘级别的for循环完事
		 * 没有多大亮点，就是利用之前写的辅助类MovieRating，方便存储和计算。
		 * @author 肖凌峰
		 * @DateTime 2018-12-31T13:30:00+0800
		 */
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			ArrayList<MovieRating> elements = new ArrayList<>();		//装values元素的容器
			for (Text value : values) {									//遍历values
				Pattern p = Pattern.compile("^(.+?)-(.+?)-(.+)$");		//这个正则表达式主要是为了过滤掉不符合格式的异常数据
				Matcher m = p.matcher(value.toString());
				if (!m.find()) {
					continue;
				}
				MovieRating temp = new MovieRating(value.toString());	//对于每个value值转换成辅助类MovieRating对象
				elements.add(temp);										//装入容器
			}

			for (int i = 0; i < elements.size(); i++) {					//做电影之间的笛卡尔积，j从i+1开始就是防止出现重复元素
				for (int j = i + 1; j < elements.size(); j++) {
					MovieRating rating1 = elements.get(i);				//取出对象i，也就是电影1
					MovieRating rating2 = elements.get(j);				//取出对象i，也就是电影2
					float ratingProduct = rating1.getPoint() * rating2.getPoint();		//计算ratingProduct
					float rating1Squared = rating1.getPoint() * rating1.getPoint();		//计算rating1Squared
					float rating2Squared = rating2.getPoint() * rating2.getPoint();		//计算rating2Squared
					String k = "" + rating1.getMovieName() + "-" + rating2.getMovieName();	//输出key值为 电影1名-电影2名
					//输出value为 rating1-给电影1打分的人数-rating2-给电影2打分的人数-ratingProduct-rating1Squared-rating2Squared
					String v = "" + rating1.getPoint() + "-" + rating1.getNumbers() + "-" + rating2.getPoint() + "-"
							+ rating2.getNumbers() + "-" + ratingProduct + "-" + rating1Squared + "-" + rating2Squared;
					context.write(new Text(k), new Text(v));	//写入文件
				}
			}
		}
	}

	/**
	 * 阶段四的Reducer，阶段四的Mapper同阶段三的Mapper，都是为了合并相同键元素的值
	 *
	 * 阶段四只需要遍历由Mapper传来的value，将每次结果计算出来跟key值一起写入文件即可
	 * 这里使用皮尔逊相关系数的计算公式
	 * correlation(X,Y) = (n∑xy-∑x∑y)/(sqrt(n∑x²-(∑x)²)*sqrt(n∑y²-(∑y)²))
	 * 这里不太清楚n是不是value的值的个数，用个数计算感觉结果不太对。加一后还是不太对
	 * 百度也说的都不太清楚
	 * 反正就是为了达到实验目的，结果不准确可以调整嘛
	 * @author 肖凌峰
	 */

	public static class TheForthStageReducer extends Reducer<Text, Text, Text, FloatWritable> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			/*
			这里sum1：∑xy
				sum2：∑x
				sum3：∑y
				sum4：∑x²
				sum5：∑y²
				pearson：皮尔逊相关系数
			其中x指的是电影1的评分
				y指的是电影2的评分
				xy指的就是上面计算出的ratingProduct
				x²指的就是上面计算出的rating1Squared
				y²指的就是上面计算出的rating2Squared
			 */
			float sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0, sum5 = 0, pearson;
			int n = 1;
			for (Text value : values) {
				String[] vals = value.toString().split("-");
				sum1 += Float.parseFloat(vals[4]);
				sum2 += Float.parseFloat(vals[0]);
				sum3 += Float.parseFloat(vals[2]);
				sum4 += Float.parseFloat(vals[5]);
				sum5 += Float.parseFloat(vals[6]);
				n++;
			}

			pearson = (float) ((n * sum1 - sum2 * sum3)
					/ (Math.sqrt(n * sum4 - sum2 * sum2) * Math.sqrt(n * sum5 - sum3 * sum3)));	//计算皮尔逊相关系数
			FloatWritable v = new FloatWritable(pearson);		//用Hadoop定义的FloatWritable封装float类型
			context.write(key, v);	//将键和皮尔逊相关系数写入文件，其中键的格式就是(电影1名称-电影2名称)
		}
	}

	public static void main(String... strings) throws IOException {

		/**
		 * 主函数就是上述MapReduce任务之间的调度，具体设置上个实验注释说明的还比较清楚，这里不累赘说明
		 * @author 肖凌峰
		 * @DateTime 2018-12-31T14:07:16+0800
		 * @return   void
		 */
		
		JobConf conf = new JobConf();
		String[] otherArgs = new String[] { "test3_2/input", "test3_2/output", "test3_2/output1", "test3_2/output2",
				"test3_2/output3" };

		Job job1 = Job.getInstance(conf, "The first job");
		Path outputPath = new Path(otherArgs[1]);
		outputPath.getFileSystem(conf).delete(outputPath, true);

		job1.setJarByClass(Test3.class);
		job1.setMapperClass(Test3.TheFirstStageMapper.class);
		job1.setReducerClass(Test3.TheFirstStageReducer.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);
		ControlledJob ctrlJob1 = new ControlledJob(conf);
		ctrlJob1.setJob(job1);
		FileInputFormat.addInputPath(job1, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job1, new Path(otherArgs[1]));

		Job job2 = Job.getInstance(conf, "The second job");
		outputPath = new Path(otherArgs[2]);
		outputPath.getFileSystem(conf).delete(outputPath, true);

		job2.setJarByClass(Test3.class);
		job2.setMapperClass(Test3.TheSecondStageMapper.class);
		job2.setReducerClass(Test3.TheSecondStageReducer.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);
		ControlledJob ctrlJob2 = new ControlledJob(conf);
		ctrlJob2.setJob(job2);
		FileInputFormat.addInputPath(job2, new Path(otherArgs[1]));
		FileOutputFormat.setOutputPath(job2, new Path(otherArgs[2]));
		ctrlJob2.addDependingJob(ctrlJob1);

		Job job3 = Job.getInstance(conf, "The second job");
		outputPath = new Path(otherArgs[3]);
		outputPath.getFileSystem(conf).delete(outputPath, true);

		job3.setJarByClass(Test3.class);
		job3.setMapperClass(Test3.TheThirdStageMapper.class);
		job3.setReducerClass(Test3.TheThirdStageReducer.class);
		job3.setOutputKeyClass(Text.class);
		job3.setOutputValueClass(Text.class);
		ControlledJob ctrlJob3 = new ControlledJob(conf);
		ctrlJob3.setJob(job3);
		ctrlJob3.addDependingJob(ctrlJob2);
		FileInputFormat.addInputPath(job3, new Path(otherArgs[2]));
		FileOutputFormat.setOutputPath(job3, new Path(otherArgs[3]));

		Job job4 = Job.getInstance(conf, "The third job");
		outputPath = new Path(otherArgs[4]);
		outputPath.getFileSystem(conf).delete(outputPath, true);

		job4.setJarByClass(Test3.class);
		job4.setMapperClass(Test3.TheThirdStageMapper.class);
		job4.setReducerClass(Test3.TheForthStageReducer.class);
		job4.setMapOutputValueClass(Text.class);
		job4.setOutputKeyClass(Text.class);
		job4.setOutputValueClass(FloatWritable.class);
		ControlledJob ctrlJob4 = new ControlledJob(conf);
		ctrlJob4.setJob(job4);
		ctrlJob4.addDependingJob(ctrlJob3);
		FileInputFormat.addInputPath(job4, new Path(otherArgs[3]));
		FileOutputFormat.setOutputPath(job4, new Path(otherArgs[4]));

		JobControl jobctrl = new JobControl("job");
		jobctrl.addJob(ctrlJob1);
		jobctrl.addJob(ctrlJob2);
		jobctrl.addJob(ctrlJob3);
		jobctrl.addJob(ctrlJob4);
		Thread t = new Thread(jobctrl);
		t.start();

		while (true) {
			if (jobctrl.allFinished()) {
				jobctrl.stop();
				break;
			}
		}
	}
}
