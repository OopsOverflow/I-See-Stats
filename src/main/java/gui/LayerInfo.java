package gui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.species.Species;
import model.species.SpeciesData;

public class LayerInfo extends VBox {
	
	public LayerInfo(SpeciesData data) {
		Species species = data.getSpecies();
		
		this.setPadding(new Insets(10));
		this.setSpacing(10);
		this.setStyle("-fx-background-color: lightgrey");
		
		Label title = new Label(species.scientificName);
		title.getStyleClass().add("my-title");
	
		Parent f1 = createField("Scientific Name", species.scientificName);
		Parent f2 = createField("Common Name", species.name);
		Parent f3 = createField("Order", species.order);
		Parent f4 = createField("Super Class", species.superclass);
		Parent f5 = createField("Precision", data.getPrecision() + "");
		getChildren().addAll(title, f1, f2, f3, f4, f5);
	}
	
	private Parent createField(String name, String value) {
		if(value == null)
			value = "<unknown>";
		
		BorderPane box = new BorderPane();
		
		Label lbl = new Label(name + " : ");
		lbl.setStyle("-fx-font-weight: bold");
		Label val = new Label(value);
		box.setLeft(lbl);
		box.setRight(val);
		return box;
	}
}
