package model.parser;

import model.geo.GeoHash;
import model.species.Species;
import model.species.SpeciesData;

import java.util.ArrayList;

/**
 * A parser is able to load OBIS data asynchronously.
 * Two backends are implemented:
 * <ul>
 * 	<li>{@link JasonParser} - loads OBIS data from json files</li>
 * 	<li>{@link WebParser} - loads OBIS data from an API endpoint</li>
 * </ul>
 */
public interface Parser {

	/**
	 * Loads species data using the provided settings.
	 * @param settings - the query settings. {@code null} fields are ignored.
	 * @return a parser query that yields {@link SpeciesData} when resolved.
	 */
    public ParserQuery<SpeciesData> load(ParserSettings settings);

	/**
	 * Gets the species scientific names that the OBIS database knows about.
	 * @return a parser query that yields a list of scientific names when resolved.
	 */
    public ParserQuery<ArrayList<String>> querySpeciesNames();

	/**
	 * Gets the species scientific names that the OBIS database knows about.
	 * @return a parser query that yields a list of scientific names when resolved.
	 */
    public ParserQuery<ArrayList<Species>> querySpeciesAtGeoHash(GeoHash geohash, int maxCount);

    /**
     * Gets a {@link Species} from a scientific name.
     * @param name - the scientific name of the species to get
     * @return a parser query that yields a {@link Species} when resolved.
     */
    public ParserQuery<Species> querySpeciesByScientificName(String name);

    /**
     * Autocomplete a string in input with the closest matching species
     * @param partial - the text to autocomplete
     * @return a parser query that yields a list of scientific names when resolved.
     */
    public ParserQuery<ArrayList<Species>> autocompleteSpecies(String partial);
}
