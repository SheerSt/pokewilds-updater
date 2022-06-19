package com.pkmngen.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

/**
 *  Author: ItsLuke
 *  
 * */

public class VersionControl {

	public String localVersion = "", latestVersion = "";
	
	// TODO change this to the github url
	private String versionEndpoint = "https://github.com/SheerSt/pokemon-wilds/releases/latest/download/version.txt";
	private String changelogEndpoint = "https://github.com/SheerSt/pokemon-wilds/releases/latest/download/changelog.txt";
	
	//Extract our changelog for display
	public String getChangelog() {
		this.latestVersion = getVersion();
		String changelog = "Changelog (" + this.latestVersion + "):\n";
		try {
			URL url = new URL(changelogEndpoint);
			InputStream in = url.openStream();
			
			URLConnection connection = url.openConnection();
			connection.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
	
			while((line = reader.readLine()) != null) {
				changelog += line + "\n";
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return changelog;
	}
	
	// Different from check version, does not store the grabbed version
	// Does not alert the user if theres a new version
	public String getVersion() {
		String version = "";
		try {
			URL url = new URL(versionEndpoint);
			InputStream in = url.openStream();
			
			URLConnection connection = url.openConnection();
			connection.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
	
			while((line = reader.readLine()) != null) {
				version += line;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return version;
	}
	
	public String GetLocalVersion() {
		return this.localVersion;
	}
	
	public String GetLatestVersion() {
		return this.latestVersion;
	}
}
