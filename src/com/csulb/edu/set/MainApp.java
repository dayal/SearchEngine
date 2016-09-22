package com.csulb.edu.set;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.csulb.edu.set.ui.view.SearchOverviewController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;
	private SearchOverviewController controller;

	boolean isValidDirectory;
	String dirPath;

	/**
	 * The data as an observable list of Persons.
	 */
	private ObservableList<String> documents = FXCollections.observableArrayList();

	/**
	 * Constructor
	 */
	public MainApp() {
		// Add some sample data
		documents.add("chapter1.txt");
		documents.add("chapter2.txt");
		documents.add("chapter5.txt");
		documents.add("chapter10.txt");
		documents.add("chapter23.txt");
		documents.add("chapter56.txt");
		documents.add("chapter90.txt");
		documents.add("chapter5.txt");
		documents.add("chapter65.txt");
		documents.add("chapter1.txt");
		documents.add("chapter2.txt");
		documents.add("chapter5.txt");
		documents.add("chapter10.txt");
		documents.add("chapter23.txt");
		documents.add("chapter56.txt");
		documents.add("chapter90.txt");
		documents.add("chapter5.txt");
		documents.add("chapter65.txt");
		documents.add("chapter1.txt");
		documents.add("chapter2.txt");
		documents.add("chapter5.txt");
		documents.add("chapter10.txt");
		documents.add("chapter23.txt");
		documents.add("chapter56.txt");
		documents.add("chapter90.txt");
		documents.add("chapter5.txt");
		documents.add("chapter65.txt");
		documents.add("chapter1.txt");
		documents.add("chapter2.txt");
		documents.add("chapter5.txt");
		documents.add("chapter10.txt");
		documents.add("chapter23.txt");
		documents.add("chapter56.txt");
		documents.add("chapter90.txt");
		documents.add("chapter5.txt");
		documents.add("chapter65.txt");
	}

	@Override
	public void start(Stage primaryStage) {
		
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Search Engine");
		
		// Opens up a text input dialog box to prompt the user to enter the
		// corpus directory path.
		promptUserForDirectoryToIndex();

		

		// Now the index has been created. Initializing the search application
		// window layout
		initRootLayout();
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

		// Handles the cancel button action
		final Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
		cancel.addEventFilter(ActionEvent.ACTION, event -> {
			this.isValidDirectory = true;
			if (this.primaryStage != null && !this.primaryStage.isShowing()) {
				System.out.println("Exiting platform now!!!");
				Platform.exit();
			}
		});
		
		// Handles the cancel button action
		/*final Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
		ok.addEventFilter(ActionEvent.ACTION, event -> {
			Optional<String> result = null;
			while (!isValidDirectory) {
				result = dialog.showAndWait();
				// The Java 8 way to get the response value (with lambda
				// expression).
				result.ifPresent(dir -> {
					System.out.println("Directory entered : " + dir);
					if ((new File(dir).isDirectory())) {
						isValidDirectory = true;
						if (this.primaryStage.isShowing() && this.controller != null) {
							this.controller.createPositionalInvertedIndex(dir);
						}
					} else {
						showInvalidDirectoryAlert();
					}
				});
			}
			dirPath = result.isPresent() ? result.get() : "";
		});		*/
		
		Optional<String> result = null;
		while (!isValidDirectory) {
			result = dialog.showAndWait();
			// The Java 8 way to get the response value (with lambda
			// expression).
			result.ifPresent(dir -> {
				System.out.println("Directory entered : " + dir);
				if ((new File(dir).isDirectory())) {
					isValidDirectory = true;
					if (this.primaryStage.isShowing() && this.controller != null) {
						this.controller.createPositionalInvertedIndex(dir);
					}
				} else {
					showInvalidDirectoryAlert();
				}
			});
		}
		dirPath = result.isPresent() ? result.get() : "";
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

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("ui/view/SearchOverview.fxml"));
			rootLayout = (AnchorPane) loader.load();

			// Give the controller access to the main app.
			controller = loader.getController();
			controller.setMainApp(this);
			
			if (dirPath != null) {
				controller.createPositionalInvertedIndex(dirPath);
			}			

			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shows the person overview inside the root layout.
	 */
	/*
	 * public void showPersonOverview() { try { // Load person overview.
	 * FXMLLoader loader = new FXMLLoader();
	 * loader.setLocation(MainApp.class.getResource("view/PersonOverview.fxml"))
	 * ; AnchorPane personOverview = (AnchorPane) loader.load();
	 * 
	 * // Set person overview into the center of root layout.
	 * rootLayout.setCenter(personOverview);
	 * 
	 * // Give the controller access to the main app. PersonOverviewController
	 * controller = loader.getController(); controller.setMainApp(this); } catch
	 * (IOException e) { e.printStackTrace(); } }
	 */

	/**
	 * Returns the main stage.
	 * 
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	/**
	 * Returns the data as an observable list of Persons.
	 * 
	 * @return
	 */
	public ObservableList<String> getDocuments() {
		return documents;
	}
}