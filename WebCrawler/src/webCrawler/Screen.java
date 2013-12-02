package webCrawler;

import java.awt.CardLayout;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;


public class Screen extends JFrame {
	
	private static final long serialVersionUID = 8471170549340567609L;
	
	private JPanel panel;
	
	private JPanel submitCard;
	private JPanel progressCard;
	private JPanel resultsCard;
	
	private JPanel URLSubmitField;
	private JPanel resultsField;
	private JPanel wordSubmitField;
	private JPanel resultsList;
	private JPanel errorSpace;
	
	private JButton URLSubmitButton;
	private JButton wordSubmitButton;
	private JButton clearButton;
	private JTextField URLtextbox;
	private JTextField wordtextbox;
	
	private JScrollPane scrollPane;
	
	private JProgressBar progressbar;
	
	private Results results;
	private Set<String> URLs;
	
	private Crawler crawler;
	
	public Screen(Crawler p) throws IOException {
		this();
		crawler = p;
	}
	
	public Screen() throws IOException {
		//Non-GUI inits
		crawler = new Crawler("http://en.wikipedia.org/", this);
		results = null;
		URLs = new HashSet<String>();
		
		//Main panel init
		panel = new JPanel(new CardLayout());
		
		//Card inits
		submitCard = new JPanel();
		submitCard.setLayout(new BoxLayout(submitCard, BoxLayout.Y_AXIS));
		progressCard = new JPanel();
		resultsCard = new JPanel();
		resultsCard.setLayout(new BoxLayout(resultsCard, BoxLayout.Y_AXIS));
		
		//TODO: Error space stuff
		errorSpace = new JPanel();
		
		//Submit field-related inits
		URLSubmitField = new JPanel();
		URLSubmitField.setLayout(new BoxLayout(URLSubmitField, BoxLayout.X_AXIS));
		URLtextbox = new JTextField(30);
		URLtextbox.setText("http://en.wikipedia.org/wiki/Mandelbrot_Set");
		URLSubmitButton = new JButton("Crawl!");
		
		//Results field-related inits
		resultsField = new JPanel();
		resultsField.setLayout(new BoxLayout(resultsField, BoxLayout.Y_AXIS));
		resultsList = new JPanel(new GridLayout(0, 2));
		scrollPane = new JScrollPane(resultsList);
		scrollPane.setPreferredSize(new Dimension(640, 480));
		resultsField.setVisible(false);
		wordSubmitField = new JPanel();
		wordtextbox = new JTextField(30);
		wordtextbox.setText("Mandelbrot");
		wordSubmitButton = new JButton("Go!");
		clearButton = new JButton("Clear Results");
		
		//Progress bar inits
		progressbar = new JProgressBar(0, 100);
		progressbar.setValue(0);
		progressbar.setStringPainted(true);
		
		//Action listener inits
		URLSubmitButton.addActionListener(new URLSubmitButtonListener(this));
		wordSubmitButton.addActionListener(new wordSubmitButtonListener(this));
		clearButton.addActionListener(new ClearButtonListener(this));
		
		//Adding components to fields
		URLSubmitField.add(URLtextbox);
		URLSubmitField.add(URLSubmitButton);
		URLSubmitField.add(clearButton);
		resultsField.add(scrollPane);
		
		//Adding fields to cards
		submitCard.add(URLSubmitField);
		resultsCard.add(wordSubmitField);
		resultsCard.add(resultsField);
		progressCard.add(progressbar);
		
		//Adding cards to panel
		panel.add(submitCard);
		panel.add(progressCard);
		
		setContentPane(panel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void Update() {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				pack();
				validate();
				repaint();
				setVisible(true);
            }
		});
	}
	
	public void showError(String s) {
		resultsList.removeAll();
		progressbar.setVisible(false);
		showMessage(s);
	}
	
	public void showProgress(int current, int max, String message) {
		if(message != null) progressbar.setString(message);
		if(!progressbar.isVisible()) progressbar.setVisible(true);
		final int progress = (current*100)/max;
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressbar.setValue(progress);
            }
          });
		Update();
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
	
	void submitWord() {
		String word = wordtextbox.getText().toLowerCase();
	}
	
	void submitURL() throws IOException {
		showMessage("");
		String url = URLtextbox.getText();
		if(URLs.contains(url)) {
			//updateResults();
			return;
		}
		
		URLs.add(url);
		
		try {
			crawler.setURL(url);
			//addWords(crawler.crawl());
		} catch(IllegalArgumentException e) {
			showError("\"" + url + "\" is not a valid URL.");
		} catch(IOException e) {
			showError(e.toString());
			throw e;
		}
		Update();
	}
	
	void clear() {
		progressbar.setVisible(false);
		resultsList.removeAll();
		errorSpace.removeAll();
		results = null;
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

class wordSubmitButtonListener implements ActionListener {
	Screen screen;
	
	public wordSubmitButtonListener(Screen screen) {
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent event) {
		screen.submitWord();
	}
}

class URLSubmitButtonListener implements ActionListener {
	Screen screen;
	
	public URLSubmitButtonListener(Screen screen) {
		this.screen = screen;
	}
	
	public void actionPerformed(ActionEvent event) {
		try {
			screen.submitURL();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
