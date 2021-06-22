package model.species;

public class Species {
    public String name;
    public String scientificName;
    public String order;
    public String superclass;
    public String recordedBy;
    
    public Species(String scientificName) {
    	this.scientificName = scientificName;
    }
}
