package app;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import gui.CameraManager;
import gui.Skybox;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import model.parser.JasonParser;
import model.parser.Parser;
import model.parser.ParserException;
import model.parser.ParserSettings;
import model.species.Species;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class EarthTest extends Application {

    private Group earth;

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
            URL modelURL = this.getClass().getResource("/resources/Earth/earth.obj");
            objModelImporter.read(modelURL);
        } catch (ImportException e) {
            e.printStackTrace();
        }

        MeshView[] meshView = objModelImporter.getImport();
        Group earth = new Group(meshView);

        return earth;
    }

    private void addGeoHashes(Group earth) throws ParserException {
    	Parser parser = new JasonParser();
    	ParserSettings settings = new ParserSettings();
    	Species dolphin = new Species();
    	dolphin.name = "Delphinidae";
		settings.species = dolphin;

        final PhongMaterial redMaterial = new PhongMaterial(new Color(0.5, 0.0, 0.0, 0.1));

        SpeciesData data = parser.load(settings);

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


    @Override
    public void start(Stage primaryStage) throws FileNotFoundException, URISyntaxException {

        //Create a Pane et graph scene root for the 3D content
        Group root3D = new Group();
        Pane pane3D = new Pane(root3D);
        System.out.println();
        System.out.println(this.getClass().getResource("").getPath());
        // Load geometry
        ObjModelImporter objModelImporter = new ObjModelImporter();
        try {
            URL modelURL = this.getClass().getResource("../earth/earth.obj");
            System.out.println(modelURL);
            objModelImporter.read(modelURL);
        } catch (ImportException e) {
            System.out.println(e.getMessage());
        }
        MeshView[] meshViews = objModelImporter.getImport();
        Group earth = new Group(meshViews);
        // Draw a line

        // Draw an helix

        // Draw city on the earth


        // Add a camera group
        PerspectiveCamera camera = new PerspectiveCamera(true);
        new CameraManager(camera, pane3D, root3D);

        // Add point light
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(-180);
        light.setTranslateY(-90);
        light.setTranslateZ(-120);
        light.getScope().addAll(root3D);
        root3D.getChildren().add(light);

        // Skybox
        Skybox sky = initSkybox(camera);

        // Add ambient light
        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        ambientLight.getScope().addAll(root3D);
        root3D.getChildren().add(ambientLight);

        // Add earth
        Group earth = createEarth();
        root3D.getChildren().addAll(earth);

        // Add skybox
        Skybox sky = initSkybox(camera);
        pane3D.getChildren().addAll(sky);

        // Add geoHashes
        try {
            addGeoHashes(earth);
        }
        catch(ParserException e) {
            e.printStackTrace();
        }

        // Create scene
        Scene scene = new Scene(pane3D, 600, 600, true);
        scene.setCamera(camera);
        scene.setFill(Color.gray(0.2));

        //Add the scene to the stage and show it
        primaryStage.setTitle("Earth Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


    // From Rahel Lüthy : https://netzwerg.ch/blog/2015/03/22/javafx-3d-line/
    public Cylinder createLine(Point3D origin, Point3D target) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder line = new Cylinder(0.01f, height);

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }

    private static Skybox initSkybox(PerspectiveCamera camera){
        // Load images
        InputStream stream = EarthTest.class.getResourceAsStream("../skybox/py(2).png");
        InputStream stream1 = EarthTest.class.getResourceAsStream("../skybox/ny(2).png");
        InputStream stream2 = EarthTest.class.getResourceAsStream("../skybox/nx(2).png");
        InputStream stream3 = EarthTest.class.getResourceAsStream("../skybox/px(2).png");
        InputStream stream4 = EarthTest.class.getResourceAsStream("../skybox/pz(2).png");
        InputStream stream5 = EarthTest.class.getResourceAsStream("../skybox/nz(2).png");
        Image imageTop = new Image(stream);
        Image imageBtm = new Image(stream1);
        Image imageLeft = new Image(stream2);
        Image imageRight = new Image(stream3);
        Image imageFront = new Image(stream4);
        Image imageBack = new Image(stream5);
        return new Skybox(imageTop, imageBtm, imageLeft, imageRight, imageFront, imageBack, 2048, camera);
    }
}
