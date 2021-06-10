package model;

import java.util.ArrayList;
import java.util.Date;

public class SpeciesData {
    private int precision;
    private ArrayList<Species> species;
    private ArrayList<Region> regions;
    private Date date;

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
