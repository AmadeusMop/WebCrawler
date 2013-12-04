package webCrawler;

import java.io.IOException;

import javax.swing.SwingUtilities;

public class Main {
	
	public static void main(String[] args) throws IOException {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				Screen s;
				try {
					s = new Screen();
					s.Update();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
		});
	}

}
