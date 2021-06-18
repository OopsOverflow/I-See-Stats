package gui;

import app.EarthTest;
import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import model.Model;
import model.geo.ColorScale;
import model.geo.GeoHash;
import model.geo.Region;
import model.parser.JasonParser;
import model.parser.Parser;
import model.parser.ParserException;
import model.parser.ParserListener;
import model.parser.ParserQuery;
import model.parser.ParserSettings;
import model.parser.WebParser;
import model.species.Species;
import model.species.SpeciesData;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Controller {

    private Model model;
    private EarthScene earthScene;

    private Group root3D;
    private PerspectiveCamera camera;

    @FXML
    private Pane earthPane;

    @FXML
    private ColorPicker btnMinColor;
    @FXML
    private ColorPicker btnMaxColor;
    @FXML
    private CheckBox btnToggleColorRange;
    @FXML
    private Slider sliderColorRangeOpacity;

    @FXML
    private HBox boxColorRange;
    
    @FXML
    private TextField searchBar;
    @FXML
    private Button btnSearchAdd;
    @FXML
    private Slider sliderPrecision;
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private CheckBox btnTimeRestriction;
    @FXML
    private VBox boxTimeRestriction;

    private static Skybox initSkybox(PerspectiveCamera camera) {
        InputStream stream = EarthTest.class.getResourceAsStream("/skybox/py(2).png");
        InputStream stream1 = EarthTest.class.getResourceAsStream("/skybox/ny(2).png");
        InputStream stream2 = EarthTest.class.getResourceAsStream("/skybox/nx(2).png");
        InputStream stream3 = EarthTest.class.getResourceAsStream("/skybox/px(2).png");
        InputStream stream4 = EarthTest.class.getResourceAsStream("/skybox/pz(2).png");
        InputStream stream5 = EarthTest.class.getResourceAsStream("/skybox/nz(2).png");
        Image imageTop = new Image(stream);
        Image imageBtm = new Image(stream1);
        Image imageLeft = new Image(stream2);
        Image imageRight = new Image(stream3);
        Image imageFront = new Image(stream4);
        Image imageBack = new Image(stream5);
        return new Skybox(imageTop, imageBtm, imageLeft, imageRight, imageFront, imageBack, 2048, camera);
    }

    private void loadInitialSpeciesData() {
        Parser parser = new JasonParser();
        ParserSettings settings = new ParserSettings();
        Species dolphin = new Species("Delphinidae");
        settings.species = dolphin;
        settings.precision = 3;

        parser.load(settings)
        	.addEventListener(earthScene);
    }

    private void createEarthScene() {
        // Add point light
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(-180);
        light.setTranslateY(-90);
        light.setTranslateZ(-120);
        light.getScope().add(root3D);
        root3D.getChildren().add(light);

        // Add ambient light
        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        ambientLight.getScope().add(root3D);
        root3D.getChildren().add(ambientLight);

        // Add skybox
        Skybox sky = initSkybox(camera);
        root3D.getChildren().add(sky);

        // Add earth
        double opacity = sliderColorRangeOpacity.getValue();
        earthScene = new EarthScene(model, opacity);
        root3D.getChildren().add(earthScene);

        loadInitialSpeciesData();
        
        // update model state based on initial button states
        onColorRangeChanged(); // update the currently visible regions
        onColorRangeToggled(); // update color range widget visibility
        onToggleTimeRestriction(); // enable / disable datepickers
    }

    private void updatePaneColorRange(ArrayList<Color> colors) {
        boxColorRange.getChildren().clear();
        boxColorRange.toFront();

        double width = boxColorRange.getPrefWidth() / colors.size();
        double height = boxColorRange.getPrefHeight();

        for (Color color : colors) {
            Rectangle rect = new Rectangle(width, height, color);
            boxColorRange.getChildren().add(rect);
        }
    }

    @FXML
    private void onColorRangeToggled() {
        boolean state = btnToggleColorRange.isSelected();
        boxColorRange.setVisible(state);
    }

    @FXML
    private void onColorRangeChanged() {
        Color minColor = btnMinColor.getValue();
        Color maxColor = btnMaxColor.getValue();
        ColorScale colScale = model.getColorScale();
        colScale.setInterpolatedColors(minColor, maxColor, colScale.getColorCount());

        updatePaneColorRange(model.getColorScale().getColors());
        earthScene.updateAllRegions();
    }

    private void onOpacityChanged() {
        double opacity = sliderColorRangeOpacity.getValue();
        earthScene.setRegionsOpacity(opacity);
    }
    
    @FXML
    private void onSearchAddClicked() {
    	model.getSpecies().clear();
    	
        ParserSettings settings = new ParserSettings();
        Species species = new Species(searchBar.getText());
        settings.species = species;
        settings.precision = (int)sliderPrecision.getValue();
        
        if(btnTimeRestriction.isSelected()) {
        	settings.startDate = startDate.getValue();
        	settings.endDate = endDate.getValue();
        }
        
        model.getParser().load(settings)
        	.addEventListener(earthScene);
    }
    
    @FXML
    public void onToggleTimeRestriction() {
    	boxTimeRestriction.setDisable(!btnTimeRestriction.isSelected());
    	
    	if(btnTimeRestriction.isSelected()) {
    		if(startDate.getValue() == null)
    			startDate.setValue(LocalDate.of(1900, 1, 1));
    		if(endDate.getValue() == null)
    			endDate.setValue(LocalDate.now());
    	}
    }
    
    @FXML
    public void initialize() {
        model = new Model();
        root3D = new Group();
        camera = new PerspectiveCamera(true);
        SubScene scene = new SubScene(root3D, 500, 600);

        new CameraManager(camera, earthPane, root3D);
        scene.setCamera(camera);
        scene.setFill(Color.GREY);
        earthPane.getChildren().add(scene);
        scene.heightProperty().bind(earthPane.heightProperty());
        scene.widthProperty().bind(earthPane.widthProperty());
        createEarthScene();

        sliderColorRangeOpacity.valueProperty().addListener((_1) -> onOpacityChanged());
    }
}
