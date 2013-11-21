package webCrawler;

import java.io.IOException;
import java.util.Map;

public class Main {
	
	public static void main(String[] args) throws IOException {
		Crawler c = new Crawler("http://en.wikipedia.org/");
		Screen s = new Screen(c);
		s.Update();
	}

}
