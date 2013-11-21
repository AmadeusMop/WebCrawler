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
	private static final String ANCHOR = "<a href=\"";
	private static final int ANCHOR_LENGTH = ANCHOR.length();
	private static final int THROTTLE = 500;
	
	private URL url;
	private Map<String, Integer> words;
	private List<String> disallowed;
	
	public Crawler(String paramURL) throws IOException {
		this(new URL(paramURL));
	}
	
	public Crawler(URL paramURL) throws IOException {
		this.url = paramURL;
		this.words = null;
		this.disallowed = getDisallowedURLs();
	}
	
	public void setURL(String paramURL) throws IOException {
		setURL(new URL(paramURL));
	}
	
	public void setURL(URL paramURL) throws IOException {
		this.url = paramURL;
		this.words = null;
		this.disallowed = getDisallowedURLs();
	}
	
	public Map<String, Integer> crawl() throws IOException {
		if(words == null) {
			words = new HashMap<String, Integer>();
			int i = 1;
			List<URL> urls = getChildURLs();
			urls.add(0, url);
			System.out.println("Crawled 0 of " + urls.size() + " URLs.");
			for(URL u : urls) {
				try {
					Thread.sleep(THROTTLE);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				subCrawl(u);
				System.out.println("Crawled " + i + " of " + urls.size() + " URLs.");
				i++;
			}
		}
		return words;
	}
	
	private void subCrawl(URL paramURL) throws IOException {
		List<String> wordsList = getWords(getHTML(paramURL.openStream()));
		
		for(String word : wordsList) {
			addWord(word);
		}
	}
	
	private void addWord(String word) {
		if(words.containsKey(word)) {
			words.put(word, words.get(word)+1);
		} else {
			words.put(word, 1);
		}
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
	
	private List<String> getWords(String s) {
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
	
	private List<URL> getChildURLs() throws IOException {
		String s = getHTML(), t;
		List<URL> output = new ArrayList<URL>();
		int index = 0;
		for(int i = 0; i < MAX_CHILD_URLS; i++) {
			index = s.indexOf(ANCHOR, index+ANCHOR_LENGTH);
			if(index == -1) break;
			
			if(s.charAt(index+ANCHOR_LENGTH) == '#') continue;
			
			t = s.substring(index+ANCHOR_LENGTH, s.indexOf('\"', index+ANCHOR_LENGTH));
			if(!disallowed.contains(t)) {
				if(t.startsWith("http://")) {
					output.add(new URL(t));
				} else {
					output.add(new URL(url.getProtocol() + "://" + url.getHost() + t));
				}
			} else {
				System.out.println(t);
				i--;
			}
		}
		return output;
	}
}
