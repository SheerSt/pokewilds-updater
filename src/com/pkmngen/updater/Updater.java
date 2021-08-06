package com.pkmngen.updater;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *  Author: ItsLuke
 *  
 * */

public class Updater {
	
	private final int EOF = -1;
	private final int BUFFER_SIZE = 4096;
	private String versionFile = "version.txt";
	
	public void update(String file) {
		String connectionURL = "https://www.github.com/SheerSt/pokemon-wilds/releases/latest/download/" + file;
		
		try {
			URL url = new URL(connectionURL);
			InputStream in = url.openStream();
			
			URLConnection connection = url.openConnection();
			connection.connect();
			int fileSize = connection.getContentLength();
			
			File temp = new File("temp");
			if(!temp.exists())
				temp.mkdir();
						
			File downloadDir = new File("temp/" + file);
			if(!downloadDir.exists()) {
				downloadDir.createNewFile();
			}
			
			copyInputStreamToFileNew(in, downloadDir, fileSize, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void copyInputStreamToFileNew(InputStream source, File destination, int fileSize, String file) throws Exception {
		try {
			FileOutputStream output = FileUtils.openOutputStream(destination);
			
			try {
				byte[] buffer = new byte[BUFFER_SIZE];
				long count = 0;
				int n = 0;
				while(EOF != (n = source.read(buffer))) {
					output.write(buffer, 0, n);
					count += n;
					int percentage = (int)(count * 100L / fileSize);
					UpdaterApplication.message.setText("Downloading " + file + " @ " + percentage + "%");
					UpdaterApplication.progress.setValue(percentage);
				}
				unzip("temp/" + destination.getName(), "./");
			} finally {
				//output.close();
				IOUtils.closeQuietly(output);
				
				UpdaterApplication.progress.setValue(33);
				UpdaterApplication.message.setText("Removing temp files...");
				FileUtils.deleteDirectory(new File("temp"));
				UpdaterApplication.message.setText("Moving files to correct directories...");
				Move("pokemon-wilds-v" + UpdaterApplication.versionControl.latestVersion, "./");
				UpdaterApplication.progress.setValue(66);
				UpdaterApplication.message.setText("Updating version file...");
				updateVersionFile();
				UpdaterApplication.progress.setValue(100);
				// Update display to say finished
				UpdaterApplication.message.setText("Update completed");
				JOptionPane.showMessageDialog(null, "Game is now ready for use! Click to close updater", "Ready!", 1);
				runGame();
			}
		} finally {
			IOUtils.closeQuietly(source);
		}
	}
	
	public void unzip(String zipPath, String dest) {
		File destDir = new File(dest);
		if(!destDir.exists()) destDir.mkdir();
		
		try {
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipPath));
			ZipEntry entry = zipIn.getNextEntry();
			
			while(entry != null) {
				String path = dest + File.separator + entry.getName();
				File parent = (new File(path)).getParentFile();
				if(!parent.exists()) parent.mkdirs();
				
				if(!entry.isDirectory()) {
					extract(zipIn, path, (int) entry.getSize());
				} else {
					File dir = new File(path);
					dir.mkdir();
				}
				
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			
			zipIn.close();

		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Failed to extract game data!\n" + e.getMessage(), "Extraction Failed!", 0);
			e.printStackTrace();
		}
	}
	
	void extract(ZipInputStream zipIn, String path, int fileSize) throws IOException {
		// Update text to say extracting
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		long count = 0;
		while((read = zipIn.read(bytesIn)) != EOF) {
			bos.write(bytesIn, 0, read);
			count += read;
			int percentage = (int) (count * 100L / fileSize);
			UpdaterApplication.message.setText("Extracting " + path + " @ " + percentage + "%");
			UpdaterApplication.progress.setValue(percentage);
		}
		bos.close();
	}
	
	void Move(String source, String destination) {
		File destinationFolder = new File(destination);
		File sourceFolder = new File(source);

		if (!destinationFolder.exists())
		{
		    destinationFolder.mkdirs();
		}

		if (sourceFolder.exists() && sourceFolder.isDirectory())
		{
		    File[] listOfFiles = sourceFolder.listFiles();
		    
		    int length = listOfFiles.length;
		    int currentIndex = 0;
		    if (listOfFiles != null)
		    {
		        for (File child : listOfFiles )
		        {
		            child.renameTo(new File(destinationFolder + "\\" + child.getName()));
		        	int percentage = (currentIndex * 100) / length;
		        	currentIndex++;
		        	UpdaterApplication.message.setText("Moving " + child.getName() + " @ " + percentage + "%");
		        	UpdaterApplication.progress.setValue(percentage);
		        }

		        sourceFolder.delete();
		    }
		}
		else
		{
		    System.out.println(sourceFolder + "  Folder does not exists");
		}
	}
	
	public void runGame() throws IOException {
		String filePath = "./pokemon-wilds.jar"; //where your jar is located.
		Runtime runtime = Runtime.getRuntime();
		runtime.exec(" java -jar " + filePath);
		// Close the updater
		System.exit(0);
	}
	
	void updateVersionFile() throws Exception {
		OutputStream in = new FileOutputStream(versionFile);
		String toWrite = UpdaterApplication.versionControl.GetLatestVersion();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(in));
		writer.write(toWrite);
		
		writer.close();
	}


}
