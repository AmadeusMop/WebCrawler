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

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

@SuppressWarnings("unused")
public class CrawlerWorker extends SwingWorker<Results, String> {
	private static void failIfInterrupted() throws InterruptedException {
	    if (Thread.currentThread().isInterrupted()) {
	      throw new InterruptedException("Interrupted while crawling URLs");
	    }
	}
  
	private static final int MAX_CHILD_URLS = 30;
	private static final String ANCHOR = "<a href=\"";
	private static final int ANCHOR_LENGTH = ANCHOR.length();
	private static final int THROTTLE = 5000;
	
	private final URL url;
	private final JProgressBar progressBar;
	//private final JTextArea messagesTextArea; //TBD
	private List<String> disallowed;
	
	public CrawlerWorker(final String paramURL, final JProgressBar paramProgressBar) throws IOException {
		this(new URL(paramURL), paramProgressBar);
	}
	
	public CrawlerWorker(final URL paramURL, final JProgressBar paramProgressBar) throws IOException {
		this.url = paramURL;
		this.progressBar = paramProgressBar;
		this.disallowed = getDisallowedURLs();
	}
	
	protected Results doInBackground() throws IOException, InterruptedException  {
		final Results results = new Results();
		setProgress(0);
		publish("Initializing...");
		List<URL> urls = getChildURLs();
		urls.add(0, url);
		int i = 1, size = urls.size();
		CrawlerWorker.failIfInterrupted();
		SleepThread st;
		for(final URL u : urls) {
			CrawlerWorker.failIfInterrupted();
			publish("Crawling " + i + " of " + size + " URLs");
			//setProgress((i*100)/size);
			st = new SleepThread(THROTTLE, this);
			st.start();
			subCrawl(u, results);
			st.join();
			i++;
		}
		return results;
	}
	
	protected void process(final List<String> chunks) {
		for(final String string: chunks) {
			progressBar.setString(string);
		}
		//TODO: Message output box
	}
	
	private void subCrawl(URL paramURL, Results results) throws IOException {
		List<String> wordsList = getWords(getHTML(paramURL.openStream()));
		
		for(String word : wordsList) {
			results.addWord(paramURL, word);
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
	
	void setP(int i) {
		setProgress(i);
	}
}

class SleepThread extends Thread {
	private int time;
	private CrawlerWorker cw;
	
	public SleepThread(int t, CrawlerWorker c) {
		time = t/100;
		cw = c;
	}
	
	public void run() {
		try {
			for(int i = 1; i <= 100; i++) {
				Thread.sleep(time);
				cw.setP(i);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
