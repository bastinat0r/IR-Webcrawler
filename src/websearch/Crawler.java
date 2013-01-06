package websearch;

/**
 * @author Sebastian Mai
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.Version;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class Crawler implements Runnable{

	private  HashMap<String, Integer> doneUrls;
	
	private Directory dir;
	private Analyzer analyzer;
	private IndexWriterConfig iwc;
	private IndexWriter writer;

	public Crawler() {
		doneUrls = new HashMap<String, Integer>();
		dir = null;
	}

	public Crawler(String indexPath) {
		doneUrls = new HashMap<String, Integer>();
		dir = null;
		analyzer = null;
		iwc = null;
		try {
			dir = FSDirectory.open(new File(indexPath));
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
			iwc.setOpenMode(OpenMode.CREATE/*_OR_APPEND*/);
			
			writer = new IndexWriter(dir, iwc);
			
			
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public void indexSite(String text, String title, String url) {
		Document doc = new Document();
		Field urlField = new StringField("url", url, Field.Store.YES);
		doc.add(urlField);
		Field textField = new TextField("text", text, Field.Store.YES);
		doc.add(textField);
		Field titleField = new TextField("title", title, Field.Store.YES);
		doc.add(titleField);
		if(writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			System.out.println("adding:   " + url);
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		
		
	}
	
	public  int crawlStep(String startUrl, int depth) {
		
		ArrayList<String> toCrawl = crawlUrl(startUrl);
		
		while(depth != 0) {
			ArrayList<String> toCrawlNext = new ArrayList<String>(toCrawl.size());
			for (String url : toCrawl) {
				toCrawlNext.addAll(crawlUrl(url));
			}
			toCrawl = toCrawlNext;
			depth--;
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		return 0;
	}
	
	
	/*
	public  ArrayList<String> scrapeUrls(String html, URL url) {
		// Match something like < a href="$url"[^>]*>
		final String pattern = "<\\s*a\\s*href=\"[^\"]*\"[^>]*>";
		Matcher m = Pattern.compile(pattern).matcher(html);
		ArrayList<String> matches = new ArrayList<String>();
		while(m.find()){
			matches.add(m.group());
		}
		
		ArrayList<String> urls = new ArrayList<String>(matches.size());
		boolean debug = false;
		for(String match : matches) {
			String[] split = match.split("\"");
			if(debug)
				System.out.println("\tfound:  " + split[1]);
			
			// Matches http://
			if(split[1].startsWith("http://")) {
				urls.add(split[1]);
				if(debug)
					System.out.println("\tadding: " + split[1]);
			} else if(!split[1].contains("://")) {
				try {
					URL u = new URL(url, split[1]);
					urls.add(u.toExternalForm());
					if(debug)
						System.out.println("\tadding: " + u.toExternalForm());
				} catch (MalformedURLException e) {
					System.out.println(e);
				}
			}
		}
		return urls;
	}

	public  ArrayList<String> crawlUrl(String url) {
		System.out.println("crawling: " + url);
		if(doneUrls.containsKey(url))
			return new ArrayList<String>();
		
		String content = "";
		doneUrls.put(url, 1);
		ArrayList<String> scrapedUrls = new ArrayList<String>();
		try {
			URL site = new URL(url);
			try {
				
				Scanner sc = new Scanner(site.openStream());
				while(sc.hasNext()) {
					content = content + sc.next() + " ";
				}
				if(writer != null)
					indexText(url, content);
				scrapedUrls = scrapeUrls(content, site);
				
			} catch (IOException ioe) {
				System.out.println(ioe);
			}
		} catch (MalformedURLException e) {
			System.out.println(e);
		}
		return scrapedUrls;
	}
*/
	public ArrayList<String> crawlUrl(String url) {
		if(doneUrls.containsKey(url)) {
			return new ArrayList<String>();
		}
		doneUrls.put(url, 1);
		ArrayList<String> nextUrls = new ArrayList<String>();
		try {
			org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
			
			indexSite(doc.body().text(), doc.title(), url);
			
			Elements links = doc.select("a[href]");
			for(Element l: links) {
				System.out.println(l.attr("href"));
				String fullUrl = constructFullUrlFromHref(l.attr("href"),url);
				if(fullUrl.endsWith("/")) {
					fullUrl = fullUrl.substring(0, fullUrl.length()-1);
				}
				nextUrls.add(fullUrl);
			}
			
		} catch (MalformedURLException me) {
			System.out.println(me);
		} catch (IOException e) {
			System.out.println(e);
		}
		return nextUrls;
	}
	
	private String constructFullUrlFromHref(String href, String url) throws MalformedURLException {
		if(href.startsWith("http://")) {
			return href;
		}
		if(href.startsWith("mailto://")) {
			throw new MalformedURLException("mailto ...");
		}
		if(href.contains("#")) {
			href = href.substring(0, href.indexOf("#"));
		}
		URL fullUrl = new URL(new URL(url), href);
		return fullUrl.toString();
	}

	@Override
	public void run() {
	}
	
}
