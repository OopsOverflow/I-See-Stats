package gui;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

import app.EarthTest;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.Model;
import model.geo.ColorScale;
import model.geo.GeoHash;
import model.geo.Region;
import model.parser.JasonParser;
import model.parser.Parser;
import model.parser.ParserException;
import model.parser.ParserSettings;
import model.species.Species;
import model.species.SpeciesData;

public class Controller {

	private final Point3D lightOffset = new Point3D(-20, -20, 0);

    private Model model;
    private Animator animator;
    private EarthScene earthScene;
    private PointLight light;
    private AmbientLight ambientLight;

    private Group root3D;
    private PerspectiveCamera camera;
    private Point2D clickPos;

    private InfoPane infoPane;

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

    @FXML
    private Button btnPlay;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnStop;
    @FXML
    private Slider sliderAnimation;
    @FXML
    private Spinner<Integer> btnAnimationSteps;
    @FXML
    private Slider sliderAnimDuration;
    @FXML
    private VBox boxAnimPlayer;

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
        light = new PointLight(Color.WHITESMOKE);
        light.setConstantAttenuation(0.5);
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

    private void createEventHandlers() {
    	earthPane.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            clickPos = new Point2D(event.getSceneX(),event.getSceneY());
            if(!(event.getPickResult().getIntersectedNode() instanceof Pane))
                infoPane.hide();
        });


        earthScene.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if(event.getButton() == MouseButton.PRIMARY) {

                if(clickPos.getX()==event.getSceneX() && clickPos.getY()==event.getSceneY()) {
                    PickResult pick = event.getPickResult();
                    Point3D point = pick.getIntersectedPoint();
                    Point2D latLon = GeoHash.coordsToLatLon(point);
                    infoPane.showAt(event.getSceneX(), event.getSceneY());
                    if (pick.getIntersectedNode().getParent().getParent() instanceof EarthScene) {
                        //on earth
                        for (SpeciesData data : model.getSpeciesData()) {
                        	GeoHash selectedArea = GeoHash.fromLatLon(latLon.getX(), latLon.getY(), data.getPrecision());
                            model.getParser()
                            	.querySpeciesAtGeoHash(selectedArea, 10)
                            	.addEventListener(infoPane);
                        }
                    }

                    else {
                        //on a geohash
                        for (SpeciesData data : model.getSpeciesData()) {
                            GeoHash selectedArea = GeoHash.fromLatLon(latLon.getX(), latLon.getY(), data.getPrecision());
                            ArrayList<Region> regions = data.getRegions();
                            for(int i=0; i<regions.size(); i+=1) {
                                GeoHash geoHash = regions.get(i).getGeoHash();
                                if (geoHash.toString().equals(selectedArea.toString())) {
                                    Label name = new Label(data.getSpecies().scientificName);
                                    Label order = new Label("Order: " + data.getSpecies().order);
                                    Label superclass = new Label("Superclass: " + data.getSpecies().superclass);
                                    Label recordedBy = new Label("RecordedBy: " + data.getSpecies().recordedBy);
                                    Label number = new Label("Number: " + regions.get(i).getCount());
                                    VBox text = new VBox();
                                    text.setSpacing(10);
                                    text.getChildren().addAll(name,order,superclass,recordedBy,number);
                                    infoPane.setContent(text);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public int multiply(int number1, int number2){
        int result = 0;
        for(int i=0; i < number2; i++){
            for(int j=0; j<number1; j++){
                result++;
            }
        }
        return result;
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
            Rectangle rect = new Rectangle(width + 1, height + 1, color);
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
    private void computeAnimation() {
        Species species = model.getSpeciesByName(searchBar.getText());
        if(species == null) {
        	AlertBaker.bakeError(ParserException.Type.JSON_MALFORMED);
        }
        else {
        	ParserSettings settings = new ParserSettings();
        	settings.species = species;
        	settings.precision = (int)sliderPrecision.getValue();

    		settings.startDate = startDate.getValue();
    		settings.endDate = endDate.getValue();

    		animator.unload();
        	model.getParser().loadAnimation(settings, btnAnimationSteps.getValue())
        		.addEventListener(animator);
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
        final double low = 0.1;

        ambientLight.setColor(state ? Color.hsb(0, 0, low) : Color.WHITE);
    }

    public void setLightPos() {
        light.setTranslateY(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateX());
        light.setTranslateZ(camera.getTranslateX());
    }

    @FXML
    private void onColorRangeChanged() {
        Color minColor = btnMinColor.getValue();
        Color maxColor = btnMaxColor.getValue();
        ColorScale colScale = model.getColorScale();
        int count = btnColorCount.getValue();
        colScale.setInterpolatedColors(minColor, maxColor, count);

        earthScene.updateAllRegions();
    }

    private void onOpacityChanged() {
        double opacity = sliderColorRangeOpacity.getValue();
        earthScene.setRegionsOpacity(opacity);
    }

    private void loadSpecies(Species species) {
    	model.getSpeciesData().clear();
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

    @FXML
    private void onSearchAddClicked() {
        Species species = model.getSpeciesByName(searchBar.getText());
        if(species == null) {
        	AlertBaker.bakeError(ParserException.Type.JSON_MALFORMED);
        	searchBar.setText("");
        }
        else {
        	loadSpecies(species);
        }
    }

    @FXML
    public void onToggleTimeRestriction() {
    	boxTimeRestriction.setDisable(!btnTimeRestriction.isSelected());
    }

    @FXML
    public void initialize() {
        model = new Model();
        root3D = new Group();
        camera = new PerspectiveCamera(true);
        animator = new Animator();
        infoPane = new InfoPane();
        SubScene scene = new SubScene(root3D, 500, 600, true, SceneAntialiasing.BALANCED);

        new CameraManager(camera, earthPane, root3D);
        scene.setCamera(camera);
        scene.setFill(Color.GREY);
        earthPane.getChildren().add(scene);
        scene.heightProperty().bind(earthPane.heightProperty());
        scene.widthProperty().bind(earthPane.widthProperty());

		if(startDate.getValue() == null)
			startDate.setValue(LocalDate.of(1900, 1, 1));
		if(endDate.getValue() == null)
			endDate.setValue(LocalDate.now());

        createEarthScene();
        createEventHandlers();
        earthPane.getChildren().add(infoPane);

        // some elements of interactivity
        new AutocompleteBox(searchBar, model);

        camera.translateZProperty().bindBidirectional(sliderZoom.valueProperty());
        sliderAnimation.valueProperty().bindBidirectional(animator.progressProperty());

        animator.addListener(earthScene);
        boxAnimPlayer.disableProperty().bind(animator.loadedProperty().not());
        animator.durationProperty().bind(sliderAnimDuration.valueProperty());
        sliderAnimation.setOnDragDetected((_1) -> animator.stop());
        btnPlay.setOnMouseClicked((_1) -> animator.start());
        btnPause.setOnMouseClicked((_1) -> animator.stop());
        btnStop.setOnMouseClicked((_1) -> {
        	animator.progressProperty().set(0);
        	animator.stop();
        });

        infoPane.setOnClickAction((_1) -> loadSpecies(_1));
        sliderColorRangeOpacity.valueProperty().addListener((_1) -> onOpacityChanged());
        btnColorCount.valueProperty().addListener((_1) -> onColorRangeChanged());
        model.getColorScale().addListener((_1) -> updatePaneColorRange((ColorScale) _1));

        model.getSpeciesData().addListener((SetChangeListener.Change<? extends SpeciesData> _1) -> {
        	Platform.runLater(() -> updateLayersTab(model.getSpeciesData()));
        });
        updateLayersTab(model.getSpeciesData());
    }
}
