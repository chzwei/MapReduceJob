package chzwei.MapReduceJob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.recognition.NatureRecognition;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import weibo4j.Timeline;
import weibo4j.model.Paging;
import weibo4j.model.StatusWapper;
import weibo4j.model.WeiboException;
import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

public class MapReduceJob {

	static String [] tokens = {"2.00BGAL6Dfj3PXCfdc229f22e7WcEAB", 
		"2.00H5caVDfj3PXC0c227939c5tf3toC", "2.00ysHL8Dm6RONC119b1679baJR3RdB"};
	
	static String uidPath 	= "F:\\eclipse\\uidlist.txt";
	static String wordPath 	= "F:\\eclipse\\word.txt";
	static String weiboPath = "F:\\eclipse\\weibo\\";
	static String resPath	= "F:\\eclipse\\res\\";
	static Vector<String> wordVector 	= null;
	static Vector<String> idlList 		= null;
	static List<Map.Entry<String,Integer>> mappingList = null;
	static Map<String,Integer> map = new TreeMap<String,Integer>(); 
	static Integer pageNum 		= 5;
	static Integer idstartindex = 0;
	static Integer idNumInteger = 100;
	
	public static void main(String[] args) 
	{
		initword();
		idlList = new Vector<String>();
		readfile(uidPath);
		Integer days = 0;
		for(int tk = 0; tk < 3; ++tk)
		{
			String token = tokens[tk%3];
			for(Integer i = idstartindex; i < idstartindex+idNumInteger; ++i)
			{
				System.out.println(idlList.get(i));
				days = getTimeline(weiboPath+idlList.get(i), idlList.get(i), token);
				Segment(weiboPath+idlList.get(i));
				search(i, 10, resPath+idlList.get(i), days);
			}
			idstartindex += idNumInteger;
		}
	}
	
	//将高频而没有实际意义的词导入程序，最后在高频词中去掉这些没有实际意义的词语
	public static void initword()
	{
		wordVector = new Vector<String>();
		wordVector.add("o");
		wordVector.add("c");
		wordVector.add("null");
		wordVector.add("uj");
		wordVector.add("ul");
		wordVector.add("w");
		wordVector.add("y");
		wordVector.add("r");
		wordVector.add("p");
		wordVector.add("d");
	}
	
	//读取uid
	public static void readfile(String filePath)
	{
		File file = new File(filePath);
		if(!file.isFile())
		{
			System.out.print("不是文件！");
			return;
		}
		if(!file.exists())
		{
			System.out.print("文件不存在");
			return;
		}
		try {
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader bufferedReader = new BufferedReader(reader);
			String linetxt = null;
			while((linetxt = bufferedReader.readLine()) != null)
			{
				String id = linetxt.substring(3, 13);
				idlList.add(id);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//抓取微博数据包
	public static Integer getTimeline(String weiboPath, String id, String token) 
	{
		String access_token = token;
		Timeline tm = new Timeline();
		tm.client.setToken(access_token);
		try {
			File weoboFile = new File(weiboPath);
			BufferedWriter weiboWriter = new BufferedWriter(new FileWriter(weoboFile));
			Date beginDate = null;
		    Date endDate = null;
		    String endday = "";
		    String beginday = "";
			for(Integer j = 1; j <= pageNum; ++j)
			{
				Paging paging = new Paging(j);
				StatusWapper status = tm.getUserTimelineByUid(id, paging, 0, 0);
				if(status.getTotalNumber() == 0)
					break;
				JSONObject jsonObject = new JSONObject(status);
			    JSONArray statusesArr = jsonObject.getJSONArray("statuses");
			    for(int i = 0; i < statusesArr.length(); ++i)
			    {
				    JSONObject statusesObj = statusesArr.getJSONObject(i);
				    weiboWriter.write(statusesObj.getString("text")+'\n');
			    }
			    if(j == 1)
			    {
			    	JSONObject statusesObj = statusesArr.getJSONObject(0);
			    	beginday = getDate(statusesObj.getString("createdAt"));
			    	beginDate = (new SimpleDateFormat("yyyy-MM-dd")).parse(beginday);
			    }
			    if(j == pageNum)
			    {
			    	JSONObject statusesObj = statusesArr.getJSONObject(statusesArr.length()-1);
			    	endday = getDate(statusesObj.getString("createdAt"));
			    	endDate = (new SimpleDateFormat("yyyy-MM-dd")).parse(endday);
			    }
			}
			weiboWriter.close();
		    return CalendarDate.calInterval(endDate, beginDate, "d")+1;
		} catch (WeiboException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	//对微博进行分词，将词语存储，并且按照词频对词语进行排序
	public static void Segment(String filePath) 
	{
		File file = new File(filePath);
		if(!file.isFile())
		{
			System.out.print("不是文件！");
			return;
		}
		if(!file.exists())
		{
			System.out.print("文件不存在");
			return;
		}
		try {
			map.clear();
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader bufferedReader = new BufferedReader(reader);
			String linetxt = null;
			Integer value = 0;
			while((linetxt = bufferedReader.readLine()) != null)
			{
		        List<Term> terms = ToAnalysis.parse(linetxt);
		        new NatureRecognition(terms).recognition();
		        Term term = null ;
		        for(int k = 0; k < terms.size(); ++k)
		        {  
		        	term = terms.get(k);
		            if(map.containsKey(term.getName()))
		            {
		            	value  = map.get(term.getName());
		            	value++;
		            	map.remove(term.getName());
		            	map.put(term.getName(), value);
		            }
		            else if(!wordVector.contains(term.getNatrue().natureStr))
		            {
		            	map.put(term.getName(), 1);
					}
		        }  
			}
			
			mappingList = new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
			Collections.sort(mappingList, new Comparator<Map.Entry<String, Integer>>()
			{
				@Override
				public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2) 
				{
					return o2.getValue().compareTo(o1.getValue()); 
				}
			});
			reader.close();
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//找出关键词，即出现频率高的词语，计算发微博的频率
	public static void search(Integer index, Integer num, String resPath, Integer days) 
	{
		try 
		{
			File resFile = new File(resPath);
			BufferedWriter resWriter = new BufferedWriter(new FileWriter(resFile));
			resWriter.write("id: " + idlList.get(index) + '\n');
			resWriter.write("关键词" + "\t" + "频次" + '\n');
			Iterator<Map.Entry<String, Integer>> it = mappingList.iterator();
			for(Integer i = 0; i < num && it.hasNext();i++)
			{ 
				Entry<String, Integer> mapping = (Map.Entry<String, Integer>) it.next();
				resWriter.write(mapping.getKey()+'\t'+mapping.getValue()+'\n');
			}
			resWriter.write("抓取 " + pageNum*20 + " 条微博\n");
			DecimalFormat   df = new   java.text.DecimalFormat("#.##"); 
			if((pageNum*20*1.0)/days != 0)
				resWriter.write("频率为 " + df.format((pageNum*20*1.0)/days) + " 次/天\n");
			else 
			{
				resWriter.write("频率为 0 次/天\n");
			}
			resWriter.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String getDate(String str) 
	{
		String res = str.substring(24, 28) + '-';
		
		if(str.substring(4, 7).equals("Jan"))
			res += "01-";
		if(str.substring(4, 7).equals("Feb"))
			res += "02-";
		if(str.substring(4, 7).equals("Mar"))
			res += "03-";
		if(str.substring(4, 7).equals("Apr"))
			res += "04-";
		if(str.substring(4, 7).equals("May"))
			res += "05-";
		if(str.substring(4, 7).equals("Jun"))			
			res += "06-";
		if(str.substring(4, 7).equals("Jul"))
			res += "07-";
		if(str.substring(4, 7).equals("Aug"))
			res += "08-";
		if(str.substring(4, 7).equals("Sep"))
			res += "09-";
		if(str.substring(4, 7).equals("Oct"))
			res += "10-";
		if(str.substring(4, 7).equals("Nov"))
			res += "11-";
		if(str.substring(4, 7).equals("Dec"))
			res += "12-";
		
		res += str.substring(8, 10);
		return res;
	}
}

