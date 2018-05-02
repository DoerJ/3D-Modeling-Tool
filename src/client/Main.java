package client;
	
	// You may add files to the windowing package, but you must leave all files
	// that are already present unchanged, except for:
	// 		Main.java (this file)
	//		drawable/Drawables.java

	// Also, do not instantiate Image361 yourself.

import javafx.stage.*;
import windowing.Window361;
import windowing.drawable.Drawable;
import javafx.application.Application;

public class Main extends Application {

	public static void main(String[] args) {
        launch(args);
	}
	@Override
	public void start(Stage primaryStage) {
		Window361 window = new Window361(primaryStage);		// create a window object
		Drawable drawable = window.getDrawable();		// Drawable returns a Image361 object
		
		Client client = new Client(drawable);		// create Drawable in window
		window.setPageTurner(client);		// set up "next page" button
		client.nextPage();		// implement next page 
		
		primaryStage.show();	// show next page
	}

}
