package com.csulb.edu.set.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import com.csulb.edu.set.SimpleTokenStream;
import com.csulb.edu.set.indexes.TokenStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Contains the general utility methods used by the application.
 * All the methods in the class are declared static so as to be called easily
 *
 */
public class Utils {

	// Regex to remove the special chars from the beginning of the word
	private static String specialCharsRegexStart = "^\\W*";
	
	// Regex to remove the special chars from the end of the word
	private static String specialCharsRegexEnd = "\\W*$";

	/**
	 * Processes the word. Removes all the special characters at the beginnig and at the end of the word
	 * @param next
	 * @return
	 */
	public static String processWord(String next) {
		return next.replaceAll(specialCharsRegexStart, "").replaceAll(specialCharsRegexEnd, "").replaceAll("'", "")
				.toLowerCase();
	}

	/*
	 * Removes the hyphens present in the word
	 */
	public static String removeHyphens(String next) {
		return next.replaceAll("-", "").toLowerCase();
	}
	
	/**
	 * Processes the json files and creates a token stream on the body element's value of the json and return it to the caller
	 * 
	 * @param jsonFile
	 * @return
	 */
	public static TokenStream getTokenStreams(File jsonFile) {
		Reader reader = null;
		try {
			reader = new FileReader(jsonFile.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Creates a json parser
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(reader);

		String bodyContents = "";

		if (element.isJsonObject()) {
			JsonObject doc = element.getAsJsonObject();
			
			// Get the value of the json element "body"
			bodyContents = doc.get("body").getAsString();
		}
		
		return new SimpleTokenStream(bodyContents);
	}
	

	/**
	 * Gets the contents of the body element to be displayed on the screen
	 * The method is called when the user double clicks on the file name to display the contents of the file on the window
	 * @param docLocation
	 * @return
	 */
	public static String getDocumentText(String docLocation) {
		
		Reader reader = null;
		try {
			reader = new FileReader(docLocation);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(reader);
		
		String bodyContents = "";

		if (element.isJsonObject()) {
			JsonObject doc = element.getAsJsonObject();
			bodyContents = doc.get("body").getAsString();
		}
		
		return bodyContents;
		
	}
}
