package Analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Properties;
import java.math.BigDecimal;
//import code.CLibrary;
import com.sun.jna.Native;

import Index.Index;

public class NLPIRDelegate {

	//private static final String userDict = "userdic.txt";// 用户词典
	
	//private static String ictclasPath = System.getProperty("user.dir");
	
	//private static String dirConfigurate = "source";// 配置文件所在目录名
	
	//private static String configurate = ictclasPath + File.separator+ dirConfigurate;// 配置文件所在目录的绝对路径
	
	private static int wordLabel = 2;// 词性标注类型（北大二级标注集）
	
	private static CLibrary ictclas;// 中科院分词系统的jni接口对象
	
	private static NLPIRDelegate instance = null;
	
	private static String NlpirPath = "";
	private static String DataPath = "";
	private static String UserdicPath = "";
	
	private static Properties props = new Properties();
	
	static	{
		try{			
			props.load(Index.class.getResourceAsStream("/db.properties"));
		}catch (IOException e){
			System.out.println("没找到配置文件");
			e.printStackTrace();   
		}
		NlpirPath = props.getProperty("NlpirPath");
		DataPath = props.getProperty("DataPath");
		UserdicPath = props.getProperty("UserdicPath");
		//System.out.println(IndexStorePath);  
	}
	

	public boolean init() {
		
		//System.out.println(System.getProperty("user.dir")+"\\source\\NLPIR");
		//ictclas = (CLibrary)Native.loadLibrary(System.getProperty("user.dir")+"\\source\\NLPIR", CLibrary.class);
		
		ictclas = (CLibrary)Native.loadLibrary(NlpirPath, CLibrary.class);
		
		int init_flag = ictclas.NLPIR_Init(DataPath, 1, "0");
		String resultString = null;
		if (0 == init_flag) {
            resultString = ictclas.NLPIR_GetLastErrorMsg();
            //System.err.println("初始化失败！\n"+resultString);
            return false;
        }
		//System.out.println("初始化成功啦");
		ictclas.NLPIR_SetPOSmap(wordLabel);
		importUserDictFile(UserdicPath);// 导入用户词典
		ictclas.NLPIR_SaveTheUsrDic();// 保存用户字典
		return true;
	}
	
	public int importUserDictFile(String path) {
		//System.out.println("导入用户词典");
		return ictclas.NLPIR_ImportUserDict(path);
	}

	public String process(String source) {
		if(source == null || source.length() < 1)
			return null;
		//System.out.println(source);
		String result = ictclas.NLPIR_ParagraphProcess(source, 0);
		//System.out.println("123123123");
		return result;
	}

	public static NLPIRDelegate getDelegate() {
		if (instance == null) {
			synchronized (NLPIRDelegate.class) {
				instance = new NLPIRDelegate();
				instance.init();
			}
		}
		return instance;
	}

	public void exit() {
		//System.out.println("out");
		ictclas.NLPIR_Exit();
	}

	public static void main(String[] args) {
		String str="";
		NLPIRDelegate id = NLPIRDelegate.getDelegate();
		String result = id.process(str);
		System.out.println(result);
		id.exit();
	}

}

