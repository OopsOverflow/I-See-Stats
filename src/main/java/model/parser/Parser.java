package model.parser;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

import model.species.Species;
import model.species.SpeciesData;

/**
 * A parser is able to load OBIS data asynchronously.
 * Two backends are implemented: 
 * <ul>
 * 	<li>{@link JasonParser} - loads OBIS data from json files</li>
 * 	<li>{@link WebParser} - loads OBIS data from an API endpoint</li>
 * </ul>
 */
public abstract class Parser {

	/**
	 * Loads species data using the provided settings.
	 * @param settings - the query settings. {@code null} fields are ignored.
	 * @return a parser query that yields {@link SpeciesData} when resolved.
	 */
    public abstract ParserQuery<SpeciesData> load(ParserSettings settings);
    
    /**
     * Autocomplete a string in input with the closest matching species
     * @param partial - the text to autocomplete
     * @return a parser query that yields a list of scientific names when resolved.
     */
    public abstract ParserQuery<ArrayList<Species>> autocompleteSpecies(String partial);
    
    /**
     * Loads a range of species data. Animation will start at {@code settings.startDate}
     * and end at {@code settings.endDate}, with the number of {@code } provided.
     * @param settings - the query settings. {@code null} fields are ignored.
     * @return a parser query that yields a list of scientific names when resolved.
     */
    public ParserQuery<ArrayList<SpeciesData>> loadAnimation(ParserSettings settings, int steps) {
    	ParserQuery<ArrayList<SpeciesData>> res = new ParserQuery<ArrayList<SpeciesData>>();
    	ArrayList<SpeciesData> loadedData = new ArrayList<SpeciesData>();
    	ArrayList<ParserQuery<SpeciesData>> allQueries = new ArrayList<ParserQuery<SpeciesData>>();
    	
    	Duration delta = Duration.between(settings.startDate, settings.endDate).dividedBy(steps);
    	LocalDate date = settings.startDate;
    	LocalDate end = settings.endDate;

		for(int i = 0; i < steps; i++) {
			date = date.plus(delta);
			settings.endDate = date;
			allQueries.add(load(settings));
		}
		
		ParserListener<SpeciesData> myListener = new ParserListener<SpeciesData>() {
			@Override
			public void onSuccess(SpeciesData result) {
				loadedData.add(result);
				if(loadedData.size() == allQueries.size()) {
					res.fireSuccess(loadedData);
				}
			}

			@Override
			public void onError(ParserException e) {
				res.fireError(e);
				allQueries.clear();
			}
		};
		
		for(ParserQuery<SpeciesData> query : allQueries) {
			query.addEventListener(myListener);
		}
		
		settings.endDate = end;
		
    	return res;
    }
}
