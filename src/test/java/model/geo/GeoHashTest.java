package model.geo;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.junit.jupiter.api.Assertions.*;

class GeoHashTest {
    GeoHash geoHash = GeoHash.fromString("ezs42");

    @Test
    void fromString() {

        assertThrows(RuntimeException.class,() -> {
            GeoHash.fromString("");
        });
        assertThrows(RuntimeException.class,() -> {
            GeoHash.fromString("123a4");
        });
        assertEquals("ezs42", GeoHash.fromString("ezs42").toString());
    }

    @Test
    void getPrecision() {
        assertEquals(5, geoHash.getPrecision());
    }

    @Test
    void getString() {
        assertEquals("ezs42", geoHash.getString());
    }

    @Test
    void testToString() {
        assertEquals("ezs42", geoHash.getString());
    }

    @Test
    void getLatLon() {
        Assertions.assertEquals(42.605, geoHash.getLatLon().getX(), .001);//real expected value : 42.60498046875
        Assertions.assertEquals(-5.603, geoHash.getLatLon().getY(), .001);//real expected value : -5.60302734375
        GeoHash geoTest = GeoHash.fromString("ez");
        Assertions.assertEquals(42.188, geoTest.getLatLon().getX(), .001);//real expected value : 42.1875
        Assertions.assertEquals(-5.625, geoTest.getLatLon().getY(), .001);//real expected value : -5.625
    }

    @Test
    void getRectCoords() {
        Point3D[] points = {
                GeoHash.latLonToCoords(new Point2D(42.583 + 0.11, -5.581 + 0.11)),
                GeoHash.latLonToCoords(new Point2D(42.583 - 0.11, -5.581 + 0.11)),
                GeoHash.latLonToCoords(new Point2D(42.583 - 0.11, -5.581 - 0.11)),
                GeoHash.latLonToCoords(new Point2D(42.583 + 0.11, -5.581 - 0.11)),
        };
        assertEquals(points[0].getX(), geoHash.getRectCoords()[0].getX(), .01);
        assertEquals(points[0].getY(), geoHash.getRectCoords()[0].getY(), .01);
        assertEquals(points[0].getZ(), geoHash.getRectCoords()[0].getZ(), .01);

        assertEquals(points[1].getX(), geoHash.getRectCoords()[1].getX(), .01);
        assertEquals(points[1].getY(), geoHash.getRectCoords()[1].getY(), .01);
        assertEquals(points[1].getZ(), geoHash.getRectCoords()[1].getZ(), .01);

        assertEquals(points[2].getX(), geoHash.getRectCoords()[2].getX(), .01);
        assertEquals(points[2].getY(), geoHash.getRectCoords()[2].getY(), .01);
        assertEquals(points[2].getZ(), geoHash.getRectCoords()[2].getZ(), .01);

        assertEquals(points[3].getX(), geoHash.getRectCoords()[3].getX(), .01);
        assertEquals(points[3].getY(), geoHash.getRectCoords()[3].getY(), .01);
        assertEquals(points[3].getZ(), geoHash.getRectCoords()[3].getZ(), .01);

    }
}