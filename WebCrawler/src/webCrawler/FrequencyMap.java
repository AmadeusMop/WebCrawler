package webCrawler;

import java.util.HashMap;

public class FrequencyMap extends HashMap<String, Integer> {
	private static final long serialVersionUID = -978841158249493415L;

	public FrequencyMap() {
		super();
	}
	
	public int get(String s) {
		Integer o = super.get(s);
		if(o == null) return 0;
		return o;
	}
	
	public Integer put(String s, Integer f) {
		if(f == 0) {
			return remove(s);
		} else {
			return super.put(s, f);
		}
	}
	
	public void increment(String s, int f) {
		if(f <= 0) throw new IllegalArgumentException(Integer.toString(f));
		put(s, get(s)+f);
	}
	
	public void decrement(String s, int f) {
		if(f >= 0) throw new IllegalArgumentException(Integer.toString(f));
		put(s, get(s)-f);
	}
	
	public void increment(String s) {
		increment(s, 1);
	}
	
	public void decrement(String s) {
		decrement(s, 1);
	}
}
