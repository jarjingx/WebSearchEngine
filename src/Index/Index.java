package Index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import Analyzer.MyAnalyzer;

public class Index {

	private static String dataPath = "C:\\workspace\\WebSearchEngine\\data"; // the directory of primitive data
	private static String indexPath = "C:\\workspace\\WebSearchEngine\\index"; // the directory of index 

	private static Analyzer analyzer;
	private static Directory directory;
	private static IndexWriter writer;	
	
	// prepare for creating an index
	public static void indexPrepare() throws IOException {
		//analyzer = new MyAnalyzer(Version.LUCENE_44);
		analyzer = new StandardAnalyzer(Version.LUCENE_44);
		directory = FSDirectory.open(new File(indexPath));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		writer = new IndexWriter(directory, config);
		System.out.println("preparation for index, done!");
	}
	
	// create an index for all files in dataPath
	public static void indexCreate( ) throws IOException {
		File[] files = new File(dataPath).listFiles();
		for (File file : files) {
			System.out.println( "creating index for " + file.getName() );
			indexCreateForFile(file);
			System.out.println( "finished index for " + file.getName() );
		}
	}

	// create an index for a certain file
	public static void indexCreateForFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		final Pattern pt_title = Pattern.compile("(<title>)(.*)(</title>)");
		final Pattern pt_url = Pattern.compile("(<url>)(.*)(</url>)");
		final Pattern pt_HTML = Pattern.compile("<([^>]*)>");
		String content;
		while((content = br.readLine()) != null) {
			if(!content.equals("<doc>")) continue;
			// find an article, then create an index for it!
			org.apache.lucene.document.Document documentLucene = new org.apache.lucene.document.Document();
			StringBuffer body = new StringBuffer();
			while((content = br.readLine()) != null) {
				// remove some useless tags like <strong> </strong>
				content = deleteUselessTag( content );
				// got a blank line, ignore it!
				if(content.length() == 0) continue;
				// have come to the end, finish it!
				if(content.equals("</doc>")) break;
				// got the content of article, add it to body!
				if(content.charAt(0) != '<') {
					body.append(content);
					continue;
				}					
				// otherwise, must be with a tag 
				if(content.charAt(1) == 'm') {
					Document doc = Jsoup.parse(content);
					Elements elm = doc.select("meta");
					if(elm.size() == 0) continue;
					if(elm.attr("name").equals("keywords")) 
						documentLucene.add(new StringField("keywords", elm.attr("content"), Store.YES));									
					else if(elm.attr("name").equals("description"))
						documentLucene.add(new StringField("description", elm.attr("content"), Store.YES));
					else if(elm.attr("name").equals("publishid"))
						documentLucene.add(new StringField("publishid", elm.attr("content"), Store.YES));
					else if(elm.attr("name").equals("subjectid"))
						documentLucene.add(new StringField("subjectid", elm.attr("content"), Store.YES));
				}
				else {
					Matcher mc = pt_title.matcher(content);
					if(mc.find()){
						String strTitle = mc.group(2).trim();
						documentLucene.add(new StringField("title", strTitle, Store.YES));
					}
					mc = pt_url.matcher(content);
					if(mc.find()){
						String strUrl = mc.group(2).trim();
						documentLucene.add(new StringField("url", strUrl, Store.YES));
					}					
				}
			}
			// have gotten the complete body, then remove some extra marks
			Matcher mc = pt_HTML.matcher(body);	
			StringBuffer sb = new StringBuffer();
			boolean result = mc.find();
			while(result){
				mc.appendReplacement(sb, "");
				result = mc.find();
			}
			mc.appendTail(sb);
			body = sb;
			documentLucene.add(new TextField("body", body.toString(), Store.YES));
			writer.addDocument(documentLucene);
			writer.commit();
		}
		br.close();
		writer.close();
	}

	// remove some useless tags
	public static String deleteUselessTag( String str ) {
		StringBuffer sb = new StringBuffer( str );
		if( sb.indexOf("<a>") > -1 ) sb.delete( sb.indexOf("<a>"), sb.indexOf("<a>")+3 );
		if( sb.indexOf("</a>") > -1 ) sb.delete( sb.indexOf("</a>"), sb.indexOf("</a>")+4 );
		if( sb.indexOf("<em>") > -1 ) sb.delete( sb.indexOf("<em>"), sb.indexOf("<em>")+4 );
		if( sb.indexOf("</em>") > -1 ) sb.delete( sb.indexOf("</em>"), sb.indexOf("</em>")+5 );
		if( sb.indexOf("<span>") > -1 ) sb.delete( sb.indexOf("<span>"), sb.indexOf("<span>")+6 );
		if( sb.indexOf("</span>") > -1 ) sb.delete( sb.indexOf("</span>"), sb.indexOf("</span>")+7 );
		if( sb.indexOf("<strong>") > -1 ) sb.delete( sb.indexOf("<strong>"), sb.indexOf("<strong>")+8 );
		if( sb.indexOf("</strong>") > -1 ) sb.delete( sb.indexOf("</strong>"), sb.indexOf("</strong>")+9 );
		if( sb.indexOf("<iframe>") > -1 ) sb.delete( sb.indexOf("<iframe>"), sb.indexOf("<iframe>")+8 );
		if( sb.indexOf("</iframe>") > -1 ) sb.delete( sb.indexOf("</iframe>"), sb.indexOf("</iframe>")+9 );
		if( sb.indexOf("&nbsp") > -1 ) sb.delete( sb.indexOf("&nbsp"), sb.indexOf("&nbsp")+5 );
        return sb.toString();
    } 
 
	public static void main(String[] args) throws IOException { 
		indexPrepare();
		indexCreate();
		System.out.println("index, done");
	}
	
}
