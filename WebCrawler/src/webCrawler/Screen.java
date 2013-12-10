package webCrawler;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker.StateValue;


public class Screen extends JFrame {
	
	private static final long serialVersionUID = 8471170549340567609L;
	
	private JPanel panel;
	private CardLayout cardLayout;
	
	private JPanel submitCard;
	private JPanel progressCard;
	private JPanel resultsCard;
	
	private JPanel URLSubmitField;
	private JPanel resultsField;
	private JPanel wordSubmitField;
	private JPanel progressField;
	private JPanel resultsList;
	private JPanel errorField;
	
	private JButton URLSubmitButton;
	private JButton wordSubmitButton;
	private JButton clearButton;
	private JTextField URLtextbox;
	private JTextField wordtextbox;
	
	private JScrollPane scrollPane;
	
	private JProgressBar progressbar;
	
	private Results results;
	
	public Screen(String title) throws IOException {
		super(title);
		
		//Non-GUI inits
		results = null;
		
		//Main panel init
		cardLayout = new CardLayout();
		panel = new JPanel(cardLayout);
		
		//Card inits
		submitCard = new JPanel();
		submitCard.setLayout(new BoxLayout(submitCard, BoxLayout.Y_AXIS));
		progressCard = new JPanel();
		resultsCard = new JPanel();
		resultsCard.setLayout(new BoxLayout(resultsCard, BoxLayout.Y_AXIS));
		
		//TODO: Error space stuff
		errorField = new JPanel();
		
		//Submit field-related inits
		URLSubmitField = new JPanel();
		URLtextbox = new JTextField(30);
		URLtextbox.setText("");
		URLSubmitButton = new JButton("Crawl!");
		
		//Results field-related inits
		resultsField = new JPanel();
		resultsList = new JPanel(new GridLayout(0, 2));
		scrollPane = new JScrollPane(resultsList);
		scrollPane.setPreferredSize(new Dimension(740, 480));
		wordSubmitField = new JPanel();
		wordtextbox = new JTextField(30);
		wordtextbox.setText("");
		wordSubmitButton = new JButton("Go!");
		clearButton = new JButton("Clear Results");
		
		//Progress bar inits
		progressField = new JPanel();
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
		progressField.add(progressbar);
		wordSubmitField.add(wordtextbox);
		wordSubmitField.add(wordSubmitButton);
		wordSubmitField.add(clearButton);
		resultsField.add(scrollPane);
		
		//Adding fields to cards
		submitCard.add(URLSubmitField);
		submitCard.add(errorField);
		resultsCard.add(wordSubmitField);
		resultsCard.add(resultsField);
		progressCard.add(progressField);
		
		//Adding cards to panel
		panel.add(submitCard);
		panel.add(progressCard);
		panel.add(resultsCard);
		
		setContentPane(panel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}
	
	public void Update() {
		pack();
		validate();
		repaint();
		setVisible(true);
	}
	
	public void showProgress(int current, int max, String message) {
		if(message != null) progressbar.setString(message);
		int progress = (current*100)/max;
		progressbar.setValue(progress);
		Update();
	}
	
	private void showError(String s, Color c) {
		errorField.removeAll();
		cardLayout.first(panel);
		Label l = new Label(s);
		l.setForeground(c);
		errorField.add(l);
	}
	
	private void showError(String s) {
		showError(s, Color.red);
	}
	
	void nextCard() {
		cardLayout.next(panel);
	}
	
	void submitWord() {
		resultsList.removeAll();
		resultsList.add(new Label("URL"));
		resultsList.add(new Label("Frequency"));
		String word = wordtextbox.getText().toLowerCase();
		FrequencyMap fr = new FrequencyMap();
		for(URL url : results.getURLs()) {
			fr.put(url.toString(), results.get(url).get(word));
		}
		ValueComparator vc = new ValueComparator(fr, true);
		List<String> urlstr = new ArrayList<String>(fr.keySet());
		Collections.sort(urlstr, vc);
		for(String str : urlstr) {
			resultsList.add(new Label(str));
			resultsList.add(new Label(Integer.toString(fr.get(str))));
		}
		Update();
	}
	
	void submitURL() throws IOException {
		nextCard();
		Update();
		showProgress(0, 1, "Initializing...");
		String url = URLtextbox.getText();
		final CrawlerWorker crawler;
		
		try {
			crawler = new CrawlerWorker(url, progressbar);
		} catch(IllegalArgumentException e) {
			showError("\"" + url + "\" is not a valid URL.");
			return;
		} catch(IOException e) {
			showError(e.toString());
			throw e;
		}
		
		crawler.addPropertyChangeListener(new PropertyChangeListener() {
	      @Override
	      public void propertyChange(final PropertyChangeEvent event) {
	        switch (event.getPropertyName()) {
	        case "progress":
	          progressbar.setIndeterminate(false);
	          progressbar.setValue((Integer) event.getNewValue());
	          break;
	        case "state":
	          switch ((StateValue) event.getNewValue()) {
	          case DONE:
	            try {
					results = crawler.get();
					nextCard();
					Update();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					Throwable cause = e.getCause();
					if(cause instanceof IOException) {
						e.printStackTrace();
						showError(cause.getMessage());
						Update();
					} else {
						e.printStackTrace();
					}
				}
	            break;
	          case STARTED:
	          case PENDING:
	            progressbar.setVisible(true);
	            progressbar.setIndeterminate(true);
	            break;
	          }
	          break;
	        }
	      }
	    });
		crawler.execute();
	}
	
	void clear() {
		cardLayout.first(panel);
		resultsList.removeAll();
		errorField.removeAll();
		results = null;
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
