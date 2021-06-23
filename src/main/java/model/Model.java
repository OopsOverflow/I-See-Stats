package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.paint.Color;
import model.geo.ColorScale;
import model.parser.Parser;
import model.parser.WebParser;
import model.species.Species;
import model.species.SpeciesData;

public class Model {
	private ObservableSet<SpeciesData> data;
    private ColorScale colorScale;
    private Parser parser;
    private Map<String, Species> speciesLookup;

    public Model() {
        Color minCol = ColorScale.setOpacity(Color.GREEN, 0.5);
        Color maxCol = ColorScale.setOpacity(Color.RED, 0.5);
        colorScale = new ColorScale(0, 1000, minCol, maxCol, 10);
        colorScale.setInterpolationType(ColorScale.Interpolation.LOGARITHMIC);
        data = FXCollections.observableSet(new HashSet<SpeciesData>());
        speciesLookup = new HashMap<String, Species>();
        parser = new WebParser("https://api.obis.org/v3/");
    }

    public ColorScale getColorScale() {
        return colorScale;
    }

    public Parser getParser() {
        return parser;
    }

    /**
     * Get the list of visible data
     * @return list of species
     */
    public ObservableSet<SpeciesData> getSpeciesData() {
        return data;
    }
    
    /**
    * Registers a new species
    * @return list of species
    */
    public void registerSpecies(Species species) {
    	speciesLookup.put(species.scientificName, species);
    }
	
    /**
    * Get a species by scientificName
    * @return list of species
    */
    public Species getSpeciesByName(String scientificName) {
    	return speciesLookup.get(scientificName);
    }
}
