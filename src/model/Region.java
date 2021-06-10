package model;

import model.GeoHash;

public class Region {
    private int count;
    private GeoHash area;

    /**
     *
     * @return number of species in the geohash
     */
    public int getCount() {
        return count;
    }
}
