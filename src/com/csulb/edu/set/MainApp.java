package com.csulb.edu.set;

import java.io.IOException;
import java.util.Optional;

import com.csulb.edu.set.view.SearchOverviewController;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;

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

	/**
	 * Returns the data as an observable list of Persons.
	 * 
	 * @return
	 */
	public ObservableList<String> getDocuments() {
		return documents;
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Search Engine");
		
		promptUserForDirectoryToIndex();

		initRootLayout();

		// showPersonOverview();
	}
	
	/**
	 * Opens up a Text Dialog prompting the user to enter the path of the directory to index
	 */
	public void promptUserForDirectoryToIndex() {
		TextInputDialog dialog = new TextInputDialog("Enter the path here");
		dialog.setTitle("Index A Directory");
		//dialog.setHeaderText("Look, a Text Input Dialog");
		dialog.setContentText("Kindly enter the path of the directory to index");
		Optional<String> result = dialog.showAndWait();

		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(name -> System.out.println("Your name: " + name));
	}

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/SearchOverview.fxml"));
			rootLayout = (AnchorPane) loader.load();

			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();

			// Give the controller access to the main app.
			SearchOverviewController controller = loader.getController();
			controller.setMainApp(this);
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
		launch(args);
	}
}