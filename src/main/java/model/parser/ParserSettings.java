package model.parser;

import model.geo.GeoHash;
import model.species.Species;

import java.time.LocalDate;

public class ParserSettings {
    public Species species;
    public int precision;
    public LocalDate startDate;
    public LocalDate endDate;
    public GeoHash geoHash;
}
