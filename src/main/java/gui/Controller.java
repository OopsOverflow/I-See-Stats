package gui;

import app.EarthTest;
import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
import model.parser.ParserSettings;
import model.species.Species;
import model.species.SpeciesData;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Controller {

    private Model model;

    private Group currentRegions;

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


    private Group root3D;
    private PerspectiveCamera camera;

    private Map<Color, Material> materials;

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

    private MeshView createQuad(Point3D topRight, Point3D bottomRight, Point3D bottomLeft, Point3D topLeft, Material mat) {
        final float[] points = {
                (float) topRight.getX(), (float) topRight.getY(), (float) topRight.getZ(),
                (float) topLeft.getX(), (float) topLeft.getY(), (float) topLeft.getZ(),
                (float) bottomLeft.getX(), (float) bottomLeft.getY(), (float) bottomLeft.getZ(),
                (float) bottomRight.getX(), (float) bottomRight.getY(), (float) bottomRight.getZ(),
        };

        final float[] texCoords = {
                1, 1,
                1, 0,
                0, 1,
                0, 0,
        };

        final int[] faces = {
                0, 1, 1, 0, 2, 2,
                0, 1, 2, 2, 3, 3
        };

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);

        MeshView view = new MeshView(mesh);
        view.setMaterial(mat);
        return view;
    }

    private Group createEarth() {
        ObjModelImporter objModelImporter = new ObjModelImporter();
        try {
            URL modelURL = this.getClass().getResource("/earth/earth.obj");
            objModelImporter.read(modelURL);
        } catch (ImportException e) {
            e.printStackTrace();
        }

        MeshView[] meshView = objModelImporter.getImport();
        Group earth = new Group(meshView);

        return earth;
    }

    private SpeciesData getInitialSpeciesData() {
        Parser parser = new JasonParser();
        ParserSettings settings = new ParserSettings();
        Species dolphin = new Species();
        dolphin.name = "Delphinidae";
        settings.species = dolphin;

        SpeciesData data = null;
        try {
            data = parser.load(settings);
        } catch (ParserException e) {
            e.printStackTrace();
        }

        return data;
    }

    private Group createGeoHashes(SpeciesData data) {
        Group regions = new Group();
        ColorScale colScale = model.getColorScale();
        double opacity = sliderColorRangeOpacity.getValue();
        colScale.setRange(data.getMinCount(), data.getMaxCount());

        // this stores the opaque colors of the color scale and associates them
        // with a material.
        materials = new HashMap<Color, Material>();

        for (Region region : data.getRegions()) {
            Color opaque = colScale.getColor(region.getCount());
            Material mat = materials.get(opaque);

            if (mat == null) {
                Color color = ColorScale.setOpacity(opaque, opacity);
                mat = new PhongMaterial(color);
                materials.put(opaque, mat);
            }

            GeoHash hash = region.getGeoHash();
            Point3D[] points = hash.getRectCoords();
            points[0] = points[0].multiply(1.01);
            points[1] = points[1].multiply(1.01);
            points[2] = points[2].multiply(1.01);
            points[3] = points[3].multiply(1.01);

            MeshView quad = createQuad(points[0], points[1], points[2], points[3], mat);
            regions.getChildren().add(quad);
        }

        return regions;
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
        Group earth = createEarth();
        root3D.getChildren().add(earth);

        // Add regions group
        currentRegions = new Group();
        root3D.getChildren().add(currentRegions);

        // update model state based on initial button states
        onColorRangeChanged(); // update the currently visible regions
        onColorRangeToggled(); // update color range widget visibility
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

        currentRegions.getChildren().clear();

        for (SpeciesData data : model.getSpecies()) {
            currentRegions.getChildren().add(createGeoHashes(data));
        }
    }

    private void onOpacityChanged() {
        double opacity = sliderColorRangeOpacity.getValue();

        materials.forEach((key, val) -> {
            PhongMaterial mat = (PhongMaterial) val;
            Color opaque = key;
            Color col = ColorScale.setOpacity(opaque, opacity);
            mat.setDiffuseColor(col);
        });
    }

    @FXML
    public void initialize() {
        model = new Model();
        model.addSpecies(getInitialSpeciesData());

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

        // Start with drawn geos
        sliderColorRangeOpacity.setValue(1);
        onOpacityChanged();

        sliderColorRangeOpacity.valueProperty().addListener((_1) -> onOpacityChanged());
    }
}
