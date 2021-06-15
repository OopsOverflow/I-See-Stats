package model.geo;

import model.geo.GeoHash;

public class Region {
    private int count;
    private GeoHash area;

	public Region(int count, GeoHash area) {
		this.count = count;
		this.area = area;
	}

    /**
     *
     * @return number of species in the geohash
     */
    public int getCount() {
        return count;
    }

    public GeoHash getGeoHash() {
        return area;
    }
}
