package com.csulb.edu.set.ui.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.csulb.edu.set.MainApp;
import com.csulb.edu.set.PorterStemmer;
import com.csulb.edu.set.indexes.biword.BiWordIndex;
import com.csulb.edu.set.indexes.pii.PositionalInvertedIndex;
import com.csulb.edu.set.query.QueryRunner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

public class SearchOverviewController {

	/**
	 * Holds the documents returned as result of query
	 */
	private ObservableList<String> documents;
	private ObservableList<String> vocab;
	boolean isValidDirectory;
	private String dirPath;

	@FXML
	private ListView<String> listView;

	@FXML
	private TextField userQuery;

	@FXML
	private Button search;

	@FXML
	private Button findStem;

	@FXML
	private Button printVocab;

	@FXML
	private TextArea jsonBodyContents;

	private PositionalInvertedIndex pInvertedIndex;
	
	private BiWordIndex biWordIndex;

	// Reference to the main application.
	private MainApp mainApp;

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	/**
	 * Opens up a Text Dialog prompting the user to enter the path of the
	 * directory to index
	 */
	public void promptUserForDirectoryToIndex() {
		TextInputDialog dialog = new TextInputDialog("Enter the path here");
		dialog.setTitle("Index A Directory");
		dialog.setHeaderText("Kindly enter the path of the directory to index");
		dialog.setContentText("Directory Path :");
		
		final ButtonType browseButton = new ButtonType("Browse", ButtonData.OTHER);
		dialog.getDialogPane().getButtonTypes().add(browseButton);
		
		final Button browse = (Button) dialog.getDialogPane().lookupButton(browseButton);		
		browse.addEventFilter(ActionEvent.ACTION, event -> {
			DirectoryChooser chooser = new DirectoryChooser();
		    chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		    File dir = chooser.showDialog(this.mainApp.getPrimaryStage());
		    if (dir == null) {
		        return;
		    }
		    this.dirPath = Paths.get(dir.getAbsolutePath()).toString();
		    dialog.getEditor().setText(this.dirPath != null ? this.dirPath : "");
		});

		// Handles the cancel button action
		final Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
		cancel.addEventFilter(ActionEvent.ACTION, event -> {
			this.isValidDirectory = true;
			if (mainApp.getPrimaryStage() != null && !mainApp.getPrimaryStage().isShowing()) {
				System.out.println("Exiting platform now!!!");
				Platform.exit();
			}
		});

		Optional<String> result = null;
		while (!isValidDirectory) {
			result = dialog.showAndWait();
			// The Java 8 way to get the response value (with lambda
			// expression).
			result.ifPresent(dir -> {
				System.out.println("Directory entered : " + dir);
				if ((new File(dir).isDirectory())) {
					isValidDirectory = true;
					this.dirPath = dir;
					createIndexes(dir);
				} else {
					showInvalidDirectoryAlert();
				}
			});
		}
		// dirPath = result.isPresent() ? result.get() : "";
		this.isValidDirectory = false;
	}

	/**
	 * Displays an error dialog box to indicate the user that the directory path
	 * entered is invalid
	 */
	private void showInvalidDirectoryAlert() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(null);
		alert.setContentText("Invalid Directory path! Please enter a valid directory");
		alert.showAndWait();
	}

	@FXML
	private void indexNewDirectory() {
		promptUserForDirectoryToIndex();
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
			System.out.println("Searching for " + queryString);
			
			if (pInvertedIndex != null && biWordIndex != null) {
				if (!documents.isEmpty())
					documents.clear();				
				documents.addAll(QueryRunner.runQueries(queryString, pInvertedIndex, biWordIndex));
				listView.setItems(documents);;
				listView.getItems().forEach(doc -> System.out.println(doc));				
			}
		}
	}

	/**
	 * Prints the vocabulary i.e all the terms in the vocabulary of the corpus,
	 * one item per line
	 */
	@FXML
	private void printVocabulary() {
		System.out.println("Printing the vocabulary");
		// Prints all the terms in the dictionary of corpus
		List<String> vocabulary = Arrays.asList(pInvertedIndex.getDictionary());
		vocabulary.forEach(word -> System.out.println(word));

		if (!vocab.isEmpty()) {
			vocab.clear();
		}
		vocab.addAll(vocabulary);
		listView.setItems(vocab);
	}

	/**
	 * Output the stem of the entered word in a new alert box
	 */
	@FXML
	private void findStem() {
		System.out.println("Findnig the stem");
		
		// Fetch the word entered by the user in the textbox 
		String word = userQuery.getText();
		if (word == null) {
			// TO-DO :: Show an error box prompting user to enter a word to stem
		} else {
			Alert stemInfo = new Alert(AlertType.INFORMATION);
			stemInfo.setTitle("Finding the stem using Porter-Stemmer Algorithm");
			stemInfo.setHeaderText("Below is the stem of the word: "+word);
			
			// Call PorterStemmer
			String stem = PorterStemmer.processToken(word);
			
			stemInfo.setContentText(stem);
			stemInfo.showAndWait();
		}
	}

	@FXML
	private void onTextFieldClick() {
		System.out.println("inside on textfield click");
		System.out.println(userQuery.isEditable());
		userQuery.setEditable(true);
		userQuery.requestFocus();
	}

	public void createIndexes(String dirPath) {
		System.out.println("In Controller :: Begin creation of index");
		// Begin indexing of all the files present at the directory location

		// TO-DO (May be) :: Make this operation happen asynchronously
		// Keep the search text box disabled till the time index is created
		// If the user clicks on the search text box, show him a message saying
		// :: Index creation in progress
		try {
			pInvertedIndex = new PositionalInvertedIndex(dirPath);
			biWordIndex = new BiWordIndex(dirPath);
			System.out.println("Indexes created successfully");
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

		listView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getClickCount() == 2) {
				SelectionModel<String> selectionModel = listView.getSelectionModel();
				String itemSelected = selectionModel.getSelectedItem();
				if (itemSelected.contains("json")) {
					this.jsonBodyContents.setText(getDocumentText(dirPath + "\\" +itemSelected));
				}
			}
		});

	}

	/**
	 * The constructor. The constructor is called before the initialize()
	 * method.
	 */
	public SearchOverviewController() {
		documents = FXCollections.observableArrayList();
		listView = new ListView<String>();
		vocab = FXCollections.observableArrayList();
		jsonBodyContents = new TextArea();
	}

	/**
	 * @return the pInvertedIndex
	 * 
	 */
	// Where is this used?
	public PositionalInvertedIndex getpInvertedIndex() {
		if (pInvertedIndex == null) {
			throw new NullPointerException("pInvertedIndex is null");
		}
		return pInvertedIndex;
	}
	
	// TODO: put this somewhere else
	private static String getDocumentText(String docLocation) {
		
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
