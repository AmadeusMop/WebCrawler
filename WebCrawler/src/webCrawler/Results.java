package webCrawler;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Results {
	private Map<URL, FrequencyMap> urls;
	
	public Results() {
		urls = new HashMap<URL, FrequencyMap>();
	}
	
	public void addURL(URL url) {
		if(urls.containsKey(url)) throw new IllegalArgumentException(url.toString());
		urls.put(url, new FrequencyMap());
	}
	
	public void addWord(URL url, String s) {
		if(!urls.containsKey(url)) addURL(url);
		FrequencyMap m = urls.get(url);
		m.increment(s);
	}
	
	public Map<URL, FrequencyMap> getURLs() {
		return urls;
	}
}
