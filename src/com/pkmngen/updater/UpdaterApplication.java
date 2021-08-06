package com.pkmngen.updater;

import java.awt.Color;
import java.io.File;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class UpdaterApplication extends JPanel {
	
	public JFrame frame = new JFrame();
	public static JProgressBar progress;
	public static JLabel message;
	public static JTextArea changelog;
	
	public static Updater updater = new Updater();
	public static VersionControl versionControl = new VersionControl();
	
	public static final String VERSION_FILE = "version.txt";
		
	public static void main(String[] args) {
		new UpdaterApplication();
	}
	
	public UpdaterApplication() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
				
		frame.setTitle("Pokemon Wilds - Updater");
		int scale = 3;
		frame.setSize(160 * scale, 144 * scale);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLayout(null);
		//setBackground(Color.black);
		
		changelog = addTextArea(10, 10, frame.getWidth() - 40, frame.getHeight() - 150);
		changelog.setText(versionControl.getChangelog());
		
		JScrollPane scroll = addScrollPane(changelog, 10, 10, frame.getWidth() - 40, frame.getHeight() - 150);
		add(scroll);
		
		progress = addProgressBar(10, frame.getHeight() - 100, frame.getWidth() - 40, 25);
		add(progress);
		
		message = addLabel("Getting latest...", 10, frame.getHeight() - 130, frame.getWidth() - 40, 25);
		add(message);
		
		frame.add(this);
		
		frame.setVisible(true);

		/*
		File f = new File(VERSION_FILE);
		try {
			if(!f.exists()) {
					updater.updateVersionFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/	
		
		String file = "pokemon-wilds-windows64";
		String os = getPlatform();
		if(os.contains("windows")) file = "pokemon-wilds-windows64";
		else if(os.contains("mac")) file = "pokemon-wilds-mac";
		updater.update(file + ".zip");
	}
	
	public JProgressBar addProgressBar(int x, int y, int width, int height) {
		JProgressBar bar = new JProgressBar();
		bar.setLocation(x, y);
		bar.setSize(width, height);
		bar.setValue(0);
		
		return bar;
	}
	
	public JLabel addLabel(String text, int x, int y, int width, int height) {
		JLabel label = new JLabel(text);
		label.setLocation(x, y);
		label.setSize(width, height);
		return label;
	}
	
	public JTextArea addTextArea(int x, int y, int width, int height) {
		JTextArea area = new JTextArea();
		area.setLocation(x, y);
		area.setSize(width, height);
		area.setLineWrap(true);
		area.setEditable(false);
		return area;
	}
	
	public JScrollPane addScrollPane(JTextArea area, int x, int y, int width, int height) {
		JScrollPane scroll = new JScrollPane(area);
		scroll.setLocation(x, y);
		scroll.setSize(width, height);
		return scroll;
	}
	
	String getPlatform() {
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		
		if(os.contains("win")) return "windows";
		else if(os.contains("mac") || os.contains("darwin")) return "mac";
		else if(os.contains("nux") || os.contains("nix") || os.contains("aix")) return "linux";
		
		return "unknown";
	}
	
}
