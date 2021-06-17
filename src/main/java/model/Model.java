package model;

import java.util.Set;
import java.util.HashSet;
import javafx.scene.paint.Color;
import model.geo.ColorScale;
import model.parser.JasonParser;
import model.parser.Parser;
import model.parser.WebParser;
import model.species.SpeciesData;

public class Model {
    private Set<SpeciesData> species;
    private ColorScale colorScale;
    private Parser parser;

    public Model() {
        Color minCol = ColorScale.setOpacity(Color.GREEN, 0.5);
        Color maxCol = ColorScale.setOpacity(Color.RED, 0.5);
        colorScale = new ColorScale(0, 1000, minCol, maxCol, 10);
        colorScale.setInterpolationType(ColorScale.Interpolation.LOGARITHMIC);
        species = new HashSet<SpeciesData>();
        parser = new WebParser("https://api.obis.org/v3/");
    }

    public ColorScale getColorScale() {
        return colorScale;
    }

    public Parser getParser() {
        return parser;
    }

    /**
     *
     * @return list of species
     */
    public Set<SpeciesData> getSpecies() {
        return species;
    }
}
