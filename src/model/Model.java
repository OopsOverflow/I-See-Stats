package model;

import java.util.ArrayList;

public class Model {
    private ArrayList<SpeciesData> species;
    private ColorScale colorScale;

    /**
     *
     * @return list of species
     */
    public ArrayList<SpeciesData> getSpecies() {
        return species;
    }

    /**
     * Add a given species
     * @param species species to add
     * @return true if the species have been added
     */
    public boolean addSpecies(SpeciesData species){
        if(this.species.contains(species))return false;
        this.species.add(species);
        return true;
    }

    /**
     * Remove a given species
     * @param species species to remove
     * @return true if the species have been removed
     */
    public boolean removeSpecies(SpeciesData species){
        if(!this.species.contains(species))return false;
        this.species.remove(species);
        return true;
    }
}
