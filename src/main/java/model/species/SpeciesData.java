package model.species;

import model.geo.Region;

import java.util.ArrayList;
import java.util.Date;

public class SpeciesData {
    private int precision;
    private Species species;
    private Date date;
    private ArrayList<Region> regions;

    public SpeciesData(int precision, Species species, Date date, ArrayList<Region> regions) {
		super();
		this.precision = precision;
		this.species = species;
		this.date = date;
		this.regions = regions;
	}

    /**
     *
     * @return minimum count of species in a region
     */
    public int getMinCount(){
        int min = Integer.MAX_VALUE;
        for(Region region : regions){
            if(region.getCount()<min)min=region.getCount();
        }
        return min;
    }

    /**
     *
     * @return maximum count of species in a region
     */
    public int getMaxCount(){
        int max = Integer.MIN_VALUE;
        for(Region region : regions){
            if(region.getCount()>max)max=region.getCount();
        }
        return max;
    }

    /**
     *
     * @return total count of species in region
     */
    public int getTotalCount(){
        int total = 0;
        for(Region region : regions){
            total+=region.getCount();
        }
        return total;
    }

    /**
     *
     * @return regions where the species are
     */
    public ArrayList<Region> getRegions() {
        return regions;
    }

    /**
     *
     * @return date of the measure
     */
    public Date getDate() {
        return date;
    }
}
