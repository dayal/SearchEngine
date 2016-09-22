package com.csulb.edu.set.ui.view;

import java.io.IOException;

import com.csulb.edu.set.MainApp;
import com.csulb.edu.set.indexes.pii.PIndexSearchEngine;
import com.csulb.edu.set.indexes.pii.PositionalInvertedIndex;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class SearchOverviewController {

	@FXML
	private ListView<String> documentsList = new ListView<String>();

	@FXML
	private TextField userQuery;

	@FXML
	private Button search;

	@FXML
	private Button quitApplication;

	@FXML
	private Button findStem;

	@FXML
	private Button printVocab;

	@FXML
	TextArea jsonBodyContents = new TextArea();

	private PositionalInvertedIndex pInvertedIndex;

	// Reference to the main application.
	private MainApp mainApp;

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

		// Add observable list data to the table
		documentsList.getItems().addAll(mainApp.getDocuments());
	}
	
	@FXML
	private void indexNewDirectory() {
		this.mainApp.promptUserForDirectoryToIndex();
	}

	/**
	 * Called when the user clicks on the search button.
	 */
	@FXML
	private void searchCorpus() {

		// Get the query entered by the user in the query text box in the
		// queryString variable
		String queryString = userQuery.getText();

		// Check if the user has actually entered a query. If it is blank ask
		// the user to enter a query
		if (queryString == null || queryString.isEmpty()) {
			// Nothing selected.
			Alert alert = new Alert(AlertType.WARNING);
			alert.initOwner(mainApp.getPrimaryStage());
			alert.setTitle("No Query Entered");
			alert.setHeaderText(
					"Oh come on!!! You don't want me to return every document in the universe. Do you think you have time to read all of that");
			alert.setContentText("Please enter the text that you want to search for in the corpus");
			alert.showAndWait();
			
			userQuery.requestFocus();
		} else {
			// Call the Query Processing API to parse the query and get the
			// tokens

			// For each token apply the Porter Stemmer Algorithm to get the stem

			// Now apply the AND, OR operations
			// Carefully check for phrase queries

			// Apply all the operations and return the list of chapters and
			// store it
			// in documentsList variable
		}
	}

	/**
	 * Called when the user wants to quit the application
	 */
	@FXML
	private void quitApplication() {
		// Quits the application and close the window
	}

	/**
	 * Prints the vocabulary i.e all the terms in the vocabulary of the corpus,
	 * one item per line
	 */
	@FXML
	private void printVocabulary() {
		// Quits the application and close the window
	}

	/**
	 * Output the stem of the entered word in a new alert box
	 */
	@FXML
	private void findStem() {
		// Quits the application and close the window
	}
	
	@FXML
	private void onTextFieldClick() {
		System.out.println("inside on textfield click");
		System.out.println(userQuery.isEditable());
		userQuery.setEditable(true);
		userQuery.requestFocus();
	}

	public void createPositionalInvertedIndex(String dirPath) {
		System.out.println("In Controller :: Begin creation of index");
		// Begin indexing of all the files present at the directory location

		// TO-DO (May be) :: Make this operation happen asynchronously
		// Keep the search text box disabled till the time index is created
		// If the user clicks on the search text box, show him a message saying
		// :: Index creation in progress
		try {
			pInvertedIndex = PIndexSearchEngine.createPositionalInvertedIndex(dirPath);
			System.out.println("Positional Inverted Index created successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		jsonBodyContents = new TextArea();

		jsonBodyContents.setText(
				"Library Services       Computers There are no public access computers in the park library, but researchers may use their own battery-powered laptop computers, provided that they do so without disturbing others in the library.  AV Equipment The library has a collection of videos, DVDs, and audiotapes reflecting park themes in its holdings. These materials may be utilized in the library.  The library has a microfilm reader and a limited collection of microfilm materials, primarily consisting of microfilm reels of the Records of the St. Louis County Court and St. Louis County Commissioners which correspond to the approximate dates that the St. Louis County government was housed in the Old Courthouse. Photocopying A fee of 15 cents per page is charged for photocopies for public researchers. Checks or cash are accepted as payment for copies. The park reserves the right to refuse to honor requests for photocopies of rare books or general materials if, in the Librarian's judgment, photocopying would be detrimental to the material or an infringement of copyright laws.  Scanning  Scans are 15 cents per page or image. Read our complete scanning policy here.  Library Catalog The library staff will be glad to provide you with information on our holdings. Researchers may also access our holdings online via a temporary website that allows the public to view cataloged titles within the National Park Service libraries. The link to the website is here.  1. Use the basic search to locate a book by title, author or subject. 2. An \"advanced\" search function is also available. There are many options within this search function. There are also several \"help\" links within the advanced search function.   This is a temporary search engine/website. Further developments will be coming in the next several months to make it easier to access NPS titles online. If you have any problem using the online catalog, please call the Jefferson National Expansion Memorial Library for assistance.   JNEM Library books do not circulate to the public, but researchers may use our books and other materials in the park library. The library does not participate in the inter-library program. The library can be reached Monday through Friday, 8 a.m. to 4:30 p.m. at (314) 655-1632.");
		// Initialize the person table with the two columns.
		/*
		 * firstNameColumn.setCellValueFactory(cellData ->
		 * cellData.getValue().firstNameProperty());
		 * lastNameColumn.setCellValueFactory(cellData ->
		 * cellData.getValue().lastNameProperty());
		 * 
		 * // Clear person details. showPersonDetails(null);
		 * 
		 * // Listen for selection changes and show the person details when //
		 * changed. personTable.getSelectionModel().selectedItemProperty()
		 * .addListener((observable, oldValue, newValue) ->
		 * showPersonDetails(newValue));
		 */
	}

	/**
	 * The constructor. The constructor is called before the initialize()
	 * method.
	 */
	public SearchOverviewController() {
	}

	/**
	 * @return the pInvertedIndex
	 */
	public PositionalInvertedIndex getpInvertedIndex() {
		if (pInvertedIndex == null) {
			throw new NullPointerException("pInvertedIndex is null");
		}
		return pInvertedIndex;
	}

}
