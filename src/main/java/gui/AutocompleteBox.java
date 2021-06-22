package gui;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import model.Model;
import model.parser.ParserException;
import model.parser.ParserListener;
import model.species.Species;

public class AutocompleteBox implements EventHandler<KeyEvent>, ParserListener<ArrayList<Species>> {
	private Model model;
	private TextField searchBar;
	private boolean requestCompleted;
	private String lastRequest;
	private ContextMenu contextMenu;

    public AutocompleteBox(TextField searchBar, Model model) {
    	this.model = model;
    	this.searchBar = searchBar;
    	this.requestCompleted = true;
    	
    	contextMenu = new ContextMenu();
    	
        MenuItem item1 = new MenuItem("About");
        MenuItem item2 = new MenuItem("Preferences");
        contextMenu.getItems().addAll(item1, item2);

        searchBar.setContextMenu(contextMenu);
         
    	searchBar.setOnKeyPressed(this);
    }

	@Override
	public void handle(KeyEvent event) {
		String text = searchBar.getText();
		sendRequest(text);
	}
	
	private void sendRequest(String text) {
		if(!requestCompleted) return;
		requestCompleted = false;
		lastRequest = text;
		
		model.getParser().autocompleteSpecies(text)
			.addEventListener(this);
	}

	@Override
	public void onSuccess(ArrayList<Species> result) {
		Platform.runLater(() -> {			
			requestCompleted = true;
			contextMenu.getItems().clear();
			
			// update dropdown
			for(Species species : result) {
				model.registerSpecies(species);
				String text = species.scientificName;
				Label label = new Label(text);
				CustomMenuItem item = new CustomMenuItem(label, true);
				item.setOnAction((_1) -> searchBar.setText(text));
				contextMenu.getItems().add(item);
			}
			
			// eventually send new request
			String text = searchBar.getText();
			if(!text.equals(lastRequest)) {
				sendRequest(text);
			}
			
			// show ctx menu
			contextMenu.show(searchBar, Side.BOTTOM, 0, 0);
		});
	}

	@Override
	public void onError(ParserException e) {
		requestCompleted = true;
		System.out.println("search failed: " + e);
	}
}