package Analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.analysis.util.CharacterUtils.CharacterBuffer;
import org.apache.lucene.util.Version;

public class MyTokenizer extends Tokenizer{  // 
	
	
	private int offset = 0, bufferIndex = -1, dataLen = 0, finalOffset = 0;
	private int current;
	private int index;
	private static final int MAX_WORD_LEN = 255;
	private static final int RE_BUFFER_SIZE = 4096;
	 
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class); // 下一个分的词
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class); // 词在原文中的位置，是为了高亮显示用的
	 
	//private final CharacterUtils charUtils;
	//private final CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);
	
	private String result;
	private String sourceStr;
	
	private char[] buffer = null;
	private int flag = 0;
	//private NLPIRDelegate nlpir;
	
	
	private final void init(Version matchVersion,Reader input){  // 初始化，有参数的，其实没啥用，这个没实现完呢撸
			init();
	}
	
	private final void init(){ // 初始化，将传进来的一篇新闻，利用NLPIR进行分词
		try{
			buffer = new char[1024];
			int cpos = 0;
			int len;
			while((len = input.read(buffer, cpos, 1024)) == 1024){
				char[] t = buffer;
				buffer = new char[buffer.length+1024];
				System.arraycopy(t, 0, buffer, 0, t.length);
				cpos += len;
			}
			cpos += len;
			
			if(cpos == -1){
				sourceStr = "";
				result = "";
				flag++;				
			}
			else{
				sourceStr = new String(buffer,0,cpos);
				//System.out.println(sourceStr);
				result = NLPIRDelegate.getDelegate().process(sourceStr);
				//System.out.println(result);
				flag ++;
			}
		}catch(IOException e){
			;
		}
	}

	public MyTokenizer(Version matchVersion, Reader input) { // yoooooo~~~~
	    super(input);
	    //charUtils = CharacterUtils.getInstance(matchVersion);
	    //init(matchVersion,input);
	}

	public MyTokenizer(Version matchVersion, AttributeFactory factory, Reader input){ // yoooooo~~~~
		super(factory, input);
		//charUtils = CharacterUtils.getInstance(matchVersion);
	    //init(matchVersion,input);
	}
	
	protected boolean isTokenChar(int c){ //这个是单字符的过滤，如果不是字母或者汉字，就过滤掉
		return Character.isLetter(c);
	}
	
	protected int normalize(int c) { // 这个啊，将所有的字母变成小写
		return Character.toLowerCase(c);
	}
	

	@Override
	  public final boolean incrementToken() throws IOException { // 这个方法很重要，参考的CharTokenizer写的
	    clearAttributes();
	    //System.out.println("nba" + index + current + sourceStr);
	    int length = 0;
	    int start = -1; // this variable is always initialized
	    int end = -1;
	    char[] buffer = termAtt.buffer();
	    while (true) {
	    	
	    	if(flag == 0){
	    		init();
	    	}
	    	if(index >= result.length()){
	    		if(length > 0){
	    			break;
	    		}else{
	    			finalOffset = correctOffset(offset);
	    			return false;
	    		}
	    	}	    	
	    	final int c = result.charAt(index);
	    	index++;
	    	current++;
	    	
	    	if(current >= sourceStr.length()){
	    		;
	    	}	    	
	    	else if(Character.isWhitespace(c) && (sourceStr.charAt(current-1) != c))
	    		current--;
	    	
	    	if(isTokenChar(c)){
	    		if(length == 0){
	    			assert start == -1;
	    			start = offset + current - 1;
	    			end = start;
	    		}else if (length >= buffer.length-1) { // check if a supplementary could run out of bounds
	    	          buffer = termAtt.resizeBuffer(2+length); // make sure a supplementary fits in the buffer
	            }
	    		end++;
	    		buffer[length] = (char) normalize(c);
	    		length++;
	    		if(length >= MAX_WORD_LEN)
	    			break;
	    	}
	    	else if(length > 0)
	    		break;
	    }
	    
	    termAtt.setLength(length);
	    assert start != -1;
	    offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
	    return true;
	}

	
	@Override
	public final void end() {  // 设置offset
		// set final offset
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException { // 这个东西，很重要，在新的一段文件进行分词之前，会调用这个文件，
		                                      //所以要在这个时候设置flag为0，然后在下一次的incrementToken的时候用init()中文分词处理
		current = 0;
		index= 0;
	    bufferIndex = 0;
	    offset = 0;
	    dataLen = 0;
	    finalOffset = 0;
	    buffer = null;
	    flag = 0;
	    //ioBuffer.reset(); // make sure to reset the IO buffer!!
	}
}
