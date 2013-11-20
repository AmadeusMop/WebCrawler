package webCrawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
	private static final int MAX_CHILD_URLS = 30;
	
	private URL url;
	private Map<String, Integer> words;
	private List<String> disallowed;
	
	public Crawler(String paramURL) throws IOException {
		this(new URL(paramURL));
	}
	
	public Crawler(URL paramURL) throws IOException {
		this.url = paramURL;
		this.words = new HashMap<String, Integer>();
		this.disallowed = getDisallowedURLs();
	}
	
	public void setURL(String paramURL) throws IOException {
		setURL(new URL(paramURL));
	}
	
	public void setURL(URL paramURL) throws IOException {
		this.url = paramURL;
		this.disallowed = getDisallowedURLs();
	}
	
	public void crawl() throws IOException {
		System.out.println(getDisallowedURLs());
		System.out.println(getChildURLs());
	}
	
	private String getHTML() throws IOException {
		return getHTML(url.openStream());
	}
	
	private String getHTML(InputStream s) throws IOException {
		byte[] barray = new byte[20];
		byte[] temp;
		int i = 0, b = s.read();
		while(b != -1) {
			if(i == barray.length) {
				temp = Arrays.copyOf(barray, i+20);
				barray = temp;
			}
			barray[i] = (byte)b;
			i++;
			b = s.read();
		}
		String str = new String(barray);
		return str;
	}
	
	private List<String> parseHTML(String s) {
		List<String> wordList = new ArrayList<String>();
		
		Pattern p = Pattern.compile("<body.*?>(.|\n|\r).*</body.*?>", Pattern.DOTALL);
		Matcher m = p.matcher(s);
		if(m.find()) {
			s = m.group();

			s = s.toLowerCase();
			s = s.replace("\n", " ");
			s = s.replaceAll("<script*?/script>", "");
			s = s.replaceAll("<.*?>", "");
			
			for(String word : s.split("[^a-zA-Z]+")) {
				wordList.add(word.toLowerCase());
			}
		} else {
		}
		return wordList;
	}
	
	private List<String> getDisallowedURLs() throws IOException {
		List<String> output = new ArrayList<String>();
		URL robotsURL = new URL("http://" + url.getHost() + "/robots.txt");
		String robots = getHTML(robotsURL.openStream());
		robots = robots.substring(robots.indexOf("User-agent: *"));
		String[] lines = robots.split("\\n+");
		
		for(String line : lines) {
			if(line.startsWith("Disallow: ")) {
				output.add(line.substring(10));
			}
		}
		
		return output;
	}
	
	private List<String> getChildURLs() throws IOException {
		String s = getHTML(), t;
		List<String> output = new ArrayList<String>();
		int index = 0;
		for(int i = 0; i < MAX_CHILD_URLS; i++) {
			index = s.indexOf("<a href=", index+10);
			if(index == -1) break;
			t = s.substring(index+9, s.indexOf('\"', index+10));
			if(!disallowed.contains(t)) {
				output.add(t);
			} else {
				i--;
			}
		}
		return output;
	}
}
