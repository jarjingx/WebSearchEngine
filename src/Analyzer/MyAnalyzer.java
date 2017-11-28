package Analyzer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.Version;

public final class MyAnalyzer extends Analyzer { 
	
	private static final String[] stopWords = {"and", "of", "the", "to", "is", "their", "can", "all", "我", "你", "他", "我们", "你们", "他们","的"};
  
    private final Version matchVersion;
  
    public MyAnalyzer(Version matchVersion) {
    	this.matchVersion = matchVersion;
    }
  
    @Override
    protected TokenStreamComponents createComponents(final String fieldName,final Reader reader) { // 利用写好的Tokenizer和已经有的filter构建Analyzer
    	
    	Tokenizer tokenizer = new MyTokenizer(Version.LUCENE_44, reader);
        
        TokenFilter lowerCaseFilter = new LowerCaseFilter(Version.LUCENE_44, tokenizer);
        TokenFilter synonymFilter = new SynonymFilter(lowerCaseFilter, getSynonymMap(), true);
        TokenFilter stopFilter = new StopFilter(Version.LUCENE_44, synonymFilter, buildCharArraySetFromArry(stopWords));
        TokenFilter stemFilter = new PorterStemFilter(stopFilter);
    	
        return new TokenStreamComponents(tokenizer, stemFilter);
    }
    
    private CharArraySet buildCharArraySetFromArry(String[] array) {
        CharArraySet set = new CharArraySet(Version.LUCENE_44, array.length, true);
        for(String value : array) {
        	set.add(value);
        }
        return set;
    }
      
      // 创建一个同义词表
    private SynonymMap getSynonymMap() {
    	String base1 = "fast";
        String syn1 = "rapid";
        
        String base2 = "slow";
        String syn2 = "sluggish";
        
        SynonymMap.Builder sb = new SynonymMap.Builder(true);
        sb.add(new CharsRef(base1), new CharsRef(syn1), true);
        sb.add(new CharsRef(base2), new CharsRef(syn2), true);
        SynonymMap smap = null;
        try{
        	smap = sb.build();
        }catch(IOException e) {
        	e.printStackTrace();
        }
        return smap;
    }
  
  
    public static void displayToken(String str,Analyzer analyzer){  // displayToken打印分词的结果，显示里面的两个属性的值，这个玩意在两个版本之中有很大改变
    	try {
    		//将一个字符串创建成Token流
    		TokenStream stream  = analyzer.tokenStream("", new StringReader(str));
    		//保存相应词汇
    		CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
    		OffsetAttribute ota = stream.addAttribute(OffsetAttribute.class);
    		stream.reset();
    		while(stream.incrementToken()){
    			System.out.print("[" + cta + "]");
    			System.out.print("[" + ota.endOffset() + "," + ota.startOffset() + "]");
    		}
    		System.out.println();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
  
  
    public static void main(String[] args) throws Exception {
    	Analyzer aly1 = new MyAnalyzer(Version.LUCENE_44);
	  
    	String str = "";
      
    	displayToken(str, aly1);
    }
}
