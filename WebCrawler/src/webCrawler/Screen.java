package webCrawler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;


public class Screen {
	
	private static final String[] stopWordsArray = {"", "a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your"};
	private static final List<String> stopWords = Arrays.asList(stopWordsArray);

	private JFrame frame;
	private JPanel panel;
	private JPanel submitField;
	private JPanel freqField;
	private JPanel optionsField;
	private JPanel wordsList;
	private JPanel errorSpace;
	private JPanel resultsField;
	private JCheckBox filterCheckbox;
	private JButton submitButton;
	private JButton clearButton;
	private JSpinner freqbox;
	private JTextField textbox;
	private JScrollPane scrollPane;
	
	private Map<String, Integer> words;
	private Set<String> URLs;
	private List<String> sortedWordsList;
	
	private Crawler crawler;
	
	private int minFreq;
	
	public Screen(Crawler p) throws IOException {
		this();
		crawler = p;
	}
	
	public Screen() throws IOException {
		crawler = new Crawler("http://en.wikipedia.org/", this);
		words = new HashMap<String, Integer>();
		minFreq = 10;
		URLs = new HashSet<String>();
		
		frame = new JFrame("Web Reader");
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		submitField = new JPanel();
		freqField = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 1));
		optionsField = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 1));
		wordsList = new JPanel(new GridLayout(0, 2));
		((GridLayout) wordsList.getLayout()).setVgap(2);
		errorSpace = new JPanel();
		resultsField = new JPanel();
		resultsField.setLayout(new BoxLayout(resultsField, BoxLayout.Y_AXIS));
		filterCheckbox = new JCheckBox("Do not show common words", true);
		submitButton = new JButton("Submit");
		clearButton = new JButton("Clear All Results");
		freqbox = new JSpinner(new SpinnerNumberModel(10, 0, 999, 1));
		textbox = new JTextField(30);
		scrollPane = new JScrollPane(wordsList);
		scrollPane.setPreferredSize(new Dimension(640, 480));
		
		submitButton.addActionListener(new URLSubmitButtonListener(this));
		clearButton.addActionListener(new ClearButtonListener(this));
		
		textbox.setText("en.wikipedia.org/wiki/Mandelbrot_Set");
		
		submitField.add(new Label("http://"));
		submitField.add(textbox);
		submitField.add(submitButton);
		freqField.add(new Label("Hide words that appear fewer than"));
		freqField.add(freqbox);
		freqField.add(new Label("times"));
		optionsField.add(filterCheckbox);
		resultsField.add(clearButton);
		resultsField.add(scrollPane);
		
		panel.add(submitField);
		panel.add(freqField);
		panel.add(optionsField);
		panel.add(errorSpace);
		panel.add(resultsField);
		
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(640, 480));
	}
	
	public void Update() {
		frame.pack();
		frame.validate();
		frame.repaint();
		frame.setVisible(true);
	}
	
	public void addWords(Map<String, Integer> hashMap) {
		errorSpace.removeAll();
		sortedWordsList = null;
		
		String s;
		int n;
		Iterator<String> iter = hashMap.keySet().iterator();
		
		while(iter.hasNext()) {
			s = iter.next();
			n = hashMap.get(s);
			if(words.containsKey(s)) {
				words.put(s, words.get(s) + n);
			} else {
				words.put(s, n);
			}
		}
		
		if(updateWordsList()) {
			showError("No words found.");
		}
	}
	
	public void setMinFreq(int f) {
		this.minFreq = f;
	}
	
	public void showError(String s) {
		wordsList.removeAll();
		showMessage(s);
	}
	
	private boolean updateWordsList() {
		wordsList.removeAll();
		if(sortedWordsList == null) {
			sortedWordsList = new ArrayList<String>(words.keySet());
			ValueComparator vc = new ValueComparator(words, true);
			Collections.sort(sortedWordsList, vc);
		}
		Iterator<String> iter = sortedWordsList.iterator();
		String s;
		int v;
		boolean empty = true, b = filterCheckbox.isSelected();
		
		while(iter.hasNext()) {
			s = iter.next();
			v = words.get(s);
			if(v < minFreq) break;
			if(b && stopWords.contains(s)) continue;
			wordsList.add(new Label(s));
			wordsList.add(new Label(Integer.toString(v)));
			empty = false;
		}

		Update();
		return empty;
	}
	
	public void showProgress(String s) {
		showMessage(s);
		Update();
		//System.out.println(s);
	}
	
	private void showMessage(String s, Color c) {
		errorSpace.removeAll();
		Label l = new Label(s);
		l.setForeground(c);
		errorSpace.add(l);
	}
	
	private void showMessage(String s) {
		showMessage(s, Color.red);
	}
	
	void submit() throws IOException {
		showMessage("");
		int f = (Integer) freqbox.getValue();
		setMinFreq(f);
		String url = "http://" + textbox.getText();
		if(URLs.contains(url)) {
			updateWordsList();
			return;
		}
		
		URLs.add(url);
		
		try {
			crawler.setURL(url);
			addWords(crawler.crawl());
		} catch(IllegalArgumentException e) {
			showError("\"" + url + "\" is not a valid URL.");
		} catch(IOException e) {
			showError(e.toString());
			throw e;
		}
		Update();
	}
	
	void clear() {
		wordsList.removeAll();
		errorSpace.removeAll();
		sortedWordsList = null;
		words = new HashMap<String, Integer>();
		URLs = new HashSet<String>();
	}
}

class ValueComparator implements Comparator<String> {

    Map<String, Integer> base;
    boolean reverse;
    
    ValueComparator(Map<String, Integer> base, boolean reverse) {
    	this.base = base;
    	this.reverse = reverse;
    }

    ValueComparator(Map<String, Integer> base) {
        this(base, false);
    }

    public int compare(String a, String b) {
        if(base.get(a).equals(base.get(b))) {
            return a.compareTo(b);
        } else {
            return base.get(a).compareTo(base.get(b))*(reverse ? -1 : 1);
        }
    }
}

class ClearButtonListener implements ActionListener {
	Screen screen;
	
	public ClearButtonListener(Screen screen) {
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent event) {
		screen.clear();
	}
}

class URLSubmitButtonListener implements ActionListener {
	Screen screen;
	
	public URLSubmitButtonListener(Screen screen) {
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent event) {
		try {
			screen.submit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
