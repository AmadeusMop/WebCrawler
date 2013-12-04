package webCrawler;

import java.util.List;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Results extends HashMap<URL, FrequencyMap> {
	
	public Results() {
		super();
	}
	
	public void addURL(URL url) {
		if(containsKey(url)) throw new IllegalArgumentException(url.toString());
		put(url, new FrequencyMap());
	}
	
	public void addWord(URL url, String s) {
		if(!containsKey(url)) addURL(url);
		FrequencyMap m = get(url);
		m.increment(s);
	}
	
	public List<URL> getURLs() {
		return new ArrayList(keySet());
	}
}
