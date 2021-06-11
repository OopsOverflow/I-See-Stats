package model.parser;

import model.geo.GeoHash;
import model.species.Species;

import java.util.Date;

public class ParserSettings {
    public Species species;
    public int precision;
    public Date startDate;
    public Date endDate;
    public GeoHash geoHash;
}
