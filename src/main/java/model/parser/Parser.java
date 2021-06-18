package model.parser;

import model.species.Species;
import model.species.SpeciesData;

import java.util.ArrayList;

public interface Parser {


    public ParserQuery<SpeciesData> load(ParserSettings settings);

    public ParserQuery<ArrayList<String>> querySpeciesNames();

    public ParserQuery<Species> querySpeciesByScientificName(String name);

//    public void onDataLoaded(Callback cb);
}
