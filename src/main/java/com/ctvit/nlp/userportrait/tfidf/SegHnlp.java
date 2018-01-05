package com.ctvit.nlp.userportrait.tfidf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public class SegHnlp {
   public String seg(String text)
   {
	   Set stopWordSet=getStopWord();
	   
	   text = text.replace((char) 12288, ' ');
  
	   text = text.trim();
	   String sSegResult = "";
	   List<Term> termList = HanLP.segment(text);
	   //System.out.println(termList);
	   for(int i=0;i<termList.size();i++)
	   {
		   if(!stopWordSet.contains(termList.get(i).word))
		   {
			   sSegResult = sSegResult + " " + termList.get(i).word;
		   }
	   }
	   sSegResult = sSegResult.replaceAll("\\pP","");//完全清除标点  
	   sSegResult = sSegResult.replaceAll("\r|\n|\t","");//完全清除标点  
	   sSegResult = sSegResult.replaceAll("&#\\d*;","");//完全清除标点  
	   sSegResult = sSegResult.replaceAll("\\d*","");//完全清除标点  
	   return sSegResult.trim();
   }
   
   public Set<String> getStopWord()
   {
       //用来存放停用词的集合
	   Set stopWordSet = new HashSet<String>();
	   try {
		   BufferedReader brStopWordTable = new BufferedReader(new FileReader("data//StopWordTable.txt"));

			//初如化停用词集
			String stopWord = null;
			try {
				while((stopWord = brStopWordTable.readLine()) != null)
				{
					stopWordSet.add(stopWord);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   return stopWordSet;
   }
   
   
   
   public static void main(String[] args)
   {
	   SegHnlp  segnlp = new SegHnlp();

	       System.out.println(segnlp.seg("　习近平12边听边记，同代表们深入14讨论。六盘水市盘州市淤泥乡岩博村党委书记余留芬发言时说，12广大农民对党的十九大报告提出土地承包到期后再延长30年的政策十分满意，习近平听了十分高兴。遵义市播州区枫香镇花茂村党总支书记潘克刚讲到乡村农家乐旅游成为乡亲致富新路，习近平说既要鼓励发展乡村农家乐，也要对乡村旅游作分析和预测，提前制定措施，确保乡村旅游可持续发展。毕节市委书记周建琨讲到把支部建在生产小组上、发展脱贫攻坚讲习所，习近平强调，新时代的农民讲习所是一个创新，党的根基在基层，一定要抓好基层党建，在农村始终坚持党的领导。黔西南州贞丰县龙场镇龙河村卫生室医生钟晶讲到农村医疗保障问题，习近平详细询问现在农民一年交多少医疗保险费、贫困乡村老百姓生产生活条件有没有改善。贵州六盘水市钟山区大湾镇海嘎村党支部第一书记杨波谈了自己连续8年坚持当驻村第一书记、带领乡亲脱贫致富的体会，习近平表示，对在脱贫攻坚一线的基层干部要关心爱护，各方面素质好、条件具备的要提拔使用，同时要鼓励年轻干部到脱贫攻坚一线去历练。习近平还对黔东南州镇远县江古镇中心小学教师黄俊琼说，老少边穷地区的教育培训工作要加大力度，让更多乡村和基层教师受到专业培训。"));
	
   }
}
