package chzwei.MapReduceJob;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.recognition.NatureRecognition;

public class ansjtest 
{
	public static void main(String[] args) throws IOException {
		String str = "@pancen @麦未萌 [哈哈]@浅唱夏陌[哈哈]谢谢//@福建省广播影视集团电视公共频道:我们都和在@美福-渝闽汇 联合推出的“夏日送美食”活动第一波中奖的8位粉丝新鲜出炉，分别是@吃喝玩乐购的小颠@小丁丁丁丁丁哥@小小天海梦@师兄a@虹虹不爱哭@我是九班的@福建真真妹妹2011@清茶宝贝a，请尽快私信联系我们，我们会以短信形式发凭证，";
		List<Term> terms = ToAnalysis.parse(str);
	    new NatureRecognition(terms).recognition() ;
//	    System.out.println(terms);
	    Term term = null ;
	    for(int i = 0; i < terms.size(); ++i)
	    {	
	    	term = terms.get(i);
	        System.out.println(term.getName()+"/"+term.getNatrue().natureStr+"|");
	    }
		
	    
	}
}
