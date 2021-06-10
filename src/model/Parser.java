package model;

import java.util.ArrayList;

public interface Parser {


    public SpeciesData load(ParserSettings settings) throws ParserException;

    public ArrayList<String> querySpeciesNames();

    public ArrayList<Species> querySpeciesByName(String name);

//    public void onDataLoaded(Callback cb);
}
