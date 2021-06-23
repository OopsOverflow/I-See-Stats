package gui;

import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import model.Model;
import model.parser.ParserException;
import model.parser.ParserListener;
import model.parser.ParserSettings;
import model.species.Species;

public class InfoPane extends Pane implements ParserListener<ArrayList<Species>> {
	
	private VBox box;
	private Consumer<Species> callback;
	
	public InfoPane() {
		box = new VBox();
		box.setSpacing(10);
		getChildren().add(box);
		setId("infoPane");
	}
	
	public void showAt(double offX, double offY) {
        setTranslateX(offX);
        setTranslateY(offY);
        toFront();
        setVisible(true);
	}
	
	public void hide() {
		setVisible(false);
		box.getChildren().clear();
	}
	
	public void setContent(Node child) {
		box.getChildren().clear();
		box.getChildren().add(child);
	}
	
	public void setOnClickAction(Consumer<Species> callback) {
		this.callback = callback;
	}

	@Override
	public void onSuccess(ArrayList<Species> result) {
		Platform.runLater(() -> {
			box.getChildren().clear();
			
			toFront();
			setVisible(true);
			
			for(Species species : result) {
				System.out.println(species.scientificName);
				Button btn = new Button(species.scientificName);
				box.getChildren().add(btn);
				
				btn.setOnAction((_1) -> {
					if(callback != null) callback.accept(species);
					hide();
				});
			}
		});
	}

	@Override
	public void onError(ParserException e) {
		AlertBaker.bakeError(e.getType());
	}
}
