package gui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import model.Model;
import model.geo.ColorScale;
import model.geo.GeoHash;
import model.geo.Region;
import model.parser.ParserException;
import model.parser.ParserListener;
import model.species.SpeciesData;

/**
 * The earth scene creates a 3D globe with geohash regions shown on top.
 */

public class EarthScene extends Group implements ParserListener<SpeciesData> {
	
	private Model model;
	private Group earth;
	private Group regions;
	private double opacity;
	private Map<Color, Material> materials;
	
	/**
	 * Creates the earth scene.
	 * @param model - the model
	 * @param opacity - the geohash regions opacityon the globe, in range [0, 1]
	 */
	public EarthScene(Model model, double opacity) {
		this.model = model;
		this.opacity = opacity;
		
		this.materials = new HashMap<Color, Material>();
		
	    earth = createEarth();
		regions = new Group();
		getChildren().addAll(earth, regions);
	}
	
	/**
	 * Changes the opacity of the the regions (geohash squares).
	 * @param opacity - the regions opacity in range [0, 1]
	 */
	public void setRegionsOpacity(double opacity) {
        materials.forEach((key, val) -> {
            PhongMaterial mat = (PhongMaterial) val;
            Color opaque = key;
            Color col = ColorScale.setOpacity(opaque, opacity);
            mat.setDiffuseColor(col);
        });
	}
	
	/**
	 * Updates the visible data on the globe according to the model state.
	 */
    public void updateAllRegions() {
        regions.getChildren().clear();

        for (SpeciesData data : model.getSpecies()) {
        	regions.getChildren().add(createGeoHashes(data));
        }
    }

	@Override
	public void onSuccess(SpeciesData result) {
		model.getSpecies().add(result);
		
		// Platform.runLater allows executing code on the ui thread.
		// onSuccess may be called asynchronously on a thread, and javafx
		// is not thread-safe.
		Platform.runLater(() -> updateAllRegions());
	}

	@Override
	public void onError(ParserException e) {
		e.printStackTrace(); // TODO: give feedback to user
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
	
    private Group createGeoHashes(SpeciesData data) {
        Group regions = new Group();
        ColorScale colScale = model.getColorScale();
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
}
