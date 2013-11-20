package webCrawler;

import java.io.IOException;

public class Main {
	
	public static void main(String[] args) throws IOException {
		Crawler c = new Crawler("http://en.wikipedia.org/");
		c.crawl();
	}

}
