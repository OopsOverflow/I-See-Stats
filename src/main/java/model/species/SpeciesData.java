package model.species;

import java.util.ArrayList;
import model.geo.Region;
import model.parser.ParserSettings;

public class SpeciesData {
    private ArrayList<Region> regions;
    private ParserSettings settings;

    public SpeciesData(ParserSettings settings, ArrayList<Region> regions) {
		super();
		this.settings = settings;
		this.regions = regions;
	}

    /**
     * Gets the geohash precision of this region
     * @return the geohash precision
     */
    public int getPrecision() {
		return settings.precision;
	}

    /**
     * Gets the species represented by this region
     * @return the species
     */
	public Species getSpecies() {
		return settings.species;
	}

    /**
     *
     * @return minimum count of species in a region
     */
    public int getMinCount() {
        if(regions.size() == 0)
          return 0;

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
    public int getMaxCount() {
        if(regions.size() == 0)
          return 0;
          
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
    public int getTotalCount() {
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
    
    public ParserSettings getParserSettings() {
    	return settings;
    }
}
