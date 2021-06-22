package gui;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Set;

import app.EarthTest;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.Model;
import model.geo.ColorScale;
import model.parser.JasonParser;
import model.parser.Parser;
import model.parser.ParserException;
import model.parser.ParserSettings;
import model.species.Species;
import model.species.SpeciesData;

public class Controller {

	private final Point3D lightOffset = new Point3D(-10, -10, 0);

    private Model model;
    private EarthScene earthScene;
    private PointLight light;
    private AmbientLight ambientLight;

    private Group root3D;
    private PerspectiveCamera camera;

    @FXML
    private Pane earthPane;

    @FXML
    private VBox layersBox;

    @FXML
    private Slider sliderZoom;
    @FXML
    private ColorPicker btnMinColor;
    @FXML
    private ColorPicker btnMaxColor;
    @FXML
    private Text textMinColor;
    @FXML
    private Text textMaxColor;
    @FXML
    private Spinner<Integer> btnColorCount;
    @FXML
    private CheckBox btnToggleColorRange;
    @FXML
    private CheckBox btnToggleHistogramView;
    @FXML
    private CheckBox btnToggleSun;
    @FXML
    private Slider sliderColorRangeOpacity;

    @FXML
    private AnchorPane boxColorRange;

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
        light = new PointLight(Color.WHITE);
        light.getScope().add(root3D);
        root3D.getChildren().add(light);

        camera.localToSceneTransformProperty().addListener((_1, _2, _3) -> {
            Point3D point = camera.localToScene(lightOffset);
            light.setTranslateX(point.getX());
            light.setTranslateY(point.getY());
            light.setTranslateZ(point.getZ());
        });

        // Add ambient light
        ambientLight = new AmbientLight(Color.WHITE);
        ambientLight.getScope().add(root3D);
        root3D.getChildren().add(ambientLight);

        // Add skybox
        Skybox sky = initSkybox(camera);
        root3D.getChildren().add(sky);

        // Add earth
        double opacity = sliderColorRangeOpacity.getValue();
        earthScene = new EarthScene(model, opacity, btnToggleHistogramView.isSelected());
        root3D.getChildren().add(earthScene);

        // update model state based on initial button states
        onSunToggled(); // update the lights
        onColorRangeChanged(); // update the currently visible regions
        onColorRangeToggled(); // update color range widget visibility
        onToggleTimeRestriction(); // enable / disable datepickers
        loadInitialSpeciesData();
    }

    private void updatePaneColorRange(ColorScale colorScale) {
        boxColorRange.getChildren().retainAll(textMinColor, textMaxColor);
        boxColorRange.toFront();

        textMinColor.setText(colorScale.getMinRange() + "");
        textMaxColor.setText(colorScale.getMaxRange() + "");

        double width = boxColorRange.getPrefWidth() / colorScale.getColorCount();
        double height = boxColorRange.getPrefHeight();

        double left = 0;

        for (Color color : colorScale.getColors()) {
            // Make rect larger than necessary; It will avoid seeing the box underneath with AA.
            // It means the last rect will overflow by one pixel, which is not noticeable.
            Rectangle rect = new Rectangle(width + 1, height, color);
            AnchorPane.setLeftAnchor(rect, left);
            left += width;
            boxColorRange.getChildren().add(rect);
        }
    }

    private void updateLayersTab(Set<SpeciesData> species) {
    	layersBox.getChildren().clear();

    	for(SpeciesData data : species) {
    		LayerInfo info = new LayerInfo(data);
    		layersBox.getChildren().add(info);
    	}
    }

    @FXML
    private void onColorRangeToggled() {
        boolean state = btnToggleColorRange.isSelected();
        boxColorRange.setVisible(state);
    }

    @FXML
    private void onHistogramViewToggled() {
        earthScene.setHistogramView(btnToggleHistogramView.isSelected());
    }

    @FXML
    private void onSunToggled() {
        boolean state = btnToggleSun.isSelected();

        light.setLightOn(state);
        final double low = 0.5;

        ambientLight.setColor(state ? Color.hsb(0, 0, low) : Color.WHITE);
    }

    public void setLightPos(){
        light.setTranslateY(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateX());
        light.setTranslateZ(camera.getTranslateX());
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
    	model.getSpeciesData().clear();

        Species species = model.getSpeciesByName(searchBar.getText());
        if(species == null) {
        	// TODO: feedback to user
        }
        else {
        	ParserSettings settings = new ParserSettings();
        	settings.species = species;
        	settings.precision = (int)sliderPrecision.getValue();

        	if(btnTimeRestriction.isSelected()) {
        		settings.startDate = startDate.getValue();
        		settings.endDate = endDate.getValue();
        	}

        	model.getParser().load(settings)
        		.addEventListener(earthScene);
        }
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
        SubScene scene = new SubScene(root3D, 500, 600, true, SceneAntialiasing.BALANCED);

        AlertBaker.bakeError(ParserException.Type.FILE_NOT_FOUND);
        new CameraManager(camera, earthPane, root3D);
        scene.setCamera(camera);
        scene.setFill(Color.GREY);
        earthPane.getChildren().add(scene);
        scene.heightProperty().bind(earthPane.heightProperty());
        scene.widthProperty().bind(earthPane.widthProperty());

        createEarthScene();

        // some elements of interactivity
        new AutocompleteBox(searchBar, model);
        camera.translateZProperty().bindBidirectional(sliderZoom.valueProperty());
        sliderColorRangeOpacity.valueProperty().addListener((_1) -> onOpacityChanged());
        btnColorCount.valueProperty().addListener((_1) -> onColorRangeChanged());
        model.getColorScale().addListener((_1) -> updatePaneColorRange((ColorScale) _1));
        model.getSpeciesData().addListener((SetChangeListener.Change<? extends SpeciesData> _1) -> {
        	Platform.runLater(() -> updateLayersTab(model.getSpeciesData()));
        });
        updateLayersTab(model.getSpeciesData());
    }
}
