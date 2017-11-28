package Index;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.highlight.*;

import Analyzer.MyAnalyzer;

public final class SearchIndex {
	
	private static String indexPath = "C:\\workspace\\WebSearchEngine\\index"; // the directory of index 
	private static Directory directory;
	private static Analyzer analyzer;
	public int nHits = 0;
	
	public ArrayList<Map<String,String>> search(String text) {
		
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		DirectoryReader reader;
		IndexSearcher searcher;
		QueryParser parser;
		Query query;
		
        try{
            directory = FSDirectory.open(new File(indexPath));
            analyzer = new StandardAnalyzer(Version.LUCENE_44);
            reader = DirectoryReader.open(directory);
            searcher = new IndexSearcher(reader);
    
            parser = new QueryParser(Version.LUCENE_44, "body", analyzer);
            query = parser.parse(text);
            
            ScoreDoc[] hits = searcher.search(query, null, 10).scoreDocs;
            
            String body;
            String highLightText = "";
            
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>"); 
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter,new QueryScorer(query));                        

            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = searcher.doc(hits[i].doc);
                
                Map<String,String> map = new HashMap<String,String>();
                map.put("url", hitDoc.get("url"));
                map.put("title", hitDoc.get("title"));
                map.put("subjectid", hitDoc.get("subjectid"));
                map.put("publishid", hitDoc.get("publishid"));
                map.put("description", hitDoc.get("description"));
                map.put("keywords", hitDoc.get("keywords"));
                body = hitDoc.get("body");
                if(body.length() > 200)
                	body = body.substring(0, 200) + "...";
                map.put("body", body);
                
                highlighter.setTextFragmenter(new SimpleFragmenter(body.length()));      
                               
                if (text != null) {                  
                    TokenStream tokenStream = analyzer.tokenStream("body",new StringReader(body));   
                    highLightText = highlighter.getBestFragment(tokenStream, body);                 
                }
                map.put("body", highLightText);
                result.add(map);
            }
            reader.close();
            directory.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }        
        return result;
    }

}
