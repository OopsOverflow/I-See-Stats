package model.geo;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ColorScaleTest {

    ColorScale colorScale = new ColorScale();

    @Test
    void getColor() {
        ArrayList<Color> test = new ArrayList<>();
        test.add(Color.LEMONCHIFFON);
        colorScale.setColors(test);
        Assertions.assertEquals(Color.LEMONCHIFFON, colorScale.getColor(0));
    }

    @Test
    void setRange() {
        Assertions.assertThrows(Exception.class, () -> {colorScale.setRange(500,200);});
    }

}