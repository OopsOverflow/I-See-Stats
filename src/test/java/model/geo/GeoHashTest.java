package model.geo;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoHashTest {
    GeoHash geoHash = GeoHash.fromString("12345");

    @Test
    void fromString() {
        Assertions.assertThrows(RuntimeException.class, () -> {GeoHash.fromString("12a3");});
    }

    @Test
    void getPrecision() {

    }

    @Test
    void getString() {
        Assertions.assertEquals(geoHash.getString(), "12345");
    }

    @Test
    void testToString() {
        Assertions.assertEquals(geoHash.toString(), "12345");
    }

    @Test
    void getLatLon() {
        Assertions.assertEquals(geoHash.getLatLon(), new Point2D(-88.2421875, -122.2119140625));
    }

    @Test
    void getRectCoords() {
    }

    @Test
    void latLonToCoords() {
    }

    @Test
    void coordsToLatLon() {
    }

    @Test
    void fromLatLon(){

    }
}