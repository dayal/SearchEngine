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

public class Utils {

	private static String specialCharsRegexStart = "^\\W*";
	private static String specialCharsRegexEnd = "\\W*$";

	public static String processWord(String next) {
		return next.replaceAll(specialCharsRegexStart, "").replaceAll(specialCharsRegexEnd, "").replaceAll("'", "")
				.toLowerCase();
	}

	public static String removeHyphens(String next) {
		return next.replaceAll("-", "").toLowerCase();
	}
	
	public static TokenStream getTokenStreams(File jsonFile) {
		Reader reader = null;
		try {
			reader = new FileReader(jsonFile.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(reader);

		String bodyContents = "";

		if (element.isJsonObject()) {
			JsonObject doc = element.getAsJsonObject();
			bodyContents = doc.get("body").getAsString();
		}
		
		// Read and process the first token from the file
		return new SimpleTokenStream(bodyContents);
	}
	

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
