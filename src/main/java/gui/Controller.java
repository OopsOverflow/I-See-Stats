package gui;

import java.io.InputStream;
import java.net.URL;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import app.EarthTest;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import model.geo.GeoHash;
import model.geo.Region;
import model.parser.JasonParser;
import model.parser.Parser;
import model.parser.ParserException;
import model.parser.ParserSettings;
import model.species.Species;
import model.species.SpeciesData;

public class Controller {

    @FXML
	private Pane earthPane;

	private Group root3D;
	private PerspectiveCamera camera;
	
    private void addQuad(Group parent, Point3D topRight, Point3D bottomRight, Point3D bottomLeft, Point3D topLeft, Material mat) {
    	final TriangleMesh mesh = new TriangleMesh();

    	final float[] points = {
    		(float)topRight.getX(),    (float)topRight.getY(),    (float)topRight.getZ(),
    		(float)topLeft.getX(),     (float)topLeft.getY(),     (float)topLeft.getZ(),
    		(float)bottomLeft.getX(),  (float)bottomLeft.getY(),  (float)bottomLeft.getZ(),
    		(float)bottomRight.getX(), (float)bottomRight.getY(), (float)bottomRight.getZ(),
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

    	mesh.getPoints().setAll(points);
    	mesh.getTexCoords().setAll(texCoords);
    	mesh.getFaces().setAll(faces);

    	final MeshView view = new MeshView(mesh);
    	view.setMaterial(mat);
    	parent.getChildren().add(view);
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
	
    private void addGeoHashes(Group earth) {
    	Parser parser = new JasonParser();
    	ParserSettings settings = new ParserSettings();
    	Species dolphin = new Species();
    	dolphin.name = "Delphinidae";
		settings.species = dolphin;

        final PhongMaterial redMaterial = new PhongMaterial(new Color(0.5, 0.0, 0.0, 0.1));

        SpeciesData data = null;
		try {
			data = parser.load(settings);
		} catch (ParserException e) {
			e.printStackTrace();
		}

        for(Region region : data.getRegions()) {
            GeoHash hash = region.getGeoHash();
            Point3D[] points = hash.getRectCoords();
            points[0] = points[0].multiply(1.01);
            points[1] = points[1].multiply(1.01);
            points[2] = points[2].multiply(1.01);
            points[3] = points[3].multiply(1.01);
            addQuad(earth, points[0], points[1], points[2], points[3], redMaterial);
        }
    }
    
    private static Skybox initSkybox(PerspectiveCamera camera){
        // Load images
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


        // Add geoHashes
        addGeoHashes(earth);
	}
	
	@FXML
	public void initialize() {
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
	}
}