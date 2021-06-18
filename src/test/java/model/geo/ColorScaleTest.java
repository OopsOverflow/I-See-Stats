package model.geo;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;



class ColorScaleTest {
    ArrayList<Color> test = new ArrayList<Color>();
    ColorScale colorScale = new ColorScale(11,12, test);

    @Test
    void getColor() {
        test.add(Color.LEMONCHIFFON);
        test.add(Color.ROYALBLUE);
        colorScale.setColors(test);
        Assertions.assertEquals(Color.LEMONCHIFFON, colorScale.getColor(0));
    }

    @Test
    void setRange() {
        Assertions.assertThrows(Exception.class, () -> {colorScale.setRange(500,200);});
        Assertions.assertThrows(Exception.class, () -> {colorScale.setRange(200,200);});

    }
    @Test
    void setColors() {
        Assertions.assertThrows(Exception.class, () -> {colorScale.setColors(test);});
    }

    @Test
    void setColorCount() {
        Assertions.assertThrows(Exception.class, () -> {colorScale.setColorCount(1);});
    }

    @Test
    void getColorCount() {
        Assertions.assertEquals(0, colorScale.getColorCount());
    }

    @Test
    void setInterpolationType() {
        colorScale.setInterpolationType(ColorScale.Interpolation.LOGARITHMIC);
        Assertions.assertEquals(ColorScale.Interpolation.LOGARITHMIC, colorScale.getInterpolationType());
    }

    @Test
    void getInterpolationType() {
        Assertions.assertEquals(ColorScale.Interpolation.LINEAR, colorScale.getInterpolationType());
    }

    @Test
    void setMinColor() {
        Assertions.assertThrows(Exception.class, () -> {colorScale.setMinColor(Color.LEMONCHIFFON);});
        test.add(Color.LEMONCHIFFON);
        test.add(Color.ROYALBLUE);
        colorScale.setColors(test);
        Assertions.assertThrows(Exception.class, () -> {colorScale.setMinColor(Color.RED);});
    }

    @Test
    void setMaxColor() {
        Assertions.assertThrows(Exception.class, () -> {colorScale.setMaxColor(Color.LEMONCHIFFON);});
        test.add(Color.LEMONCHIFFON);
        test.add(Color.ROYALBLUE);
        colorScale.setColors(test);
        Assertions.assertThrows(Exception.class, () -> {colorScale.setMaxColor(Color.RED);});
    }

    @Test
    void setInterpolatedColors() {
        Assertions.assertThrows(Exception.class, () -> {colorScale.setInterpolatedColors(Color.LEMONCHIFFON,Color.ROYALBLUE,1);});
        colorScale.setInterpolatedColors(Color.LEMONCHIFFON, Color.ROYALBLUE, 2);
        Assertions.assertEquals(Color.LEMONCHIFFON,colorScale.getColors().get(0));
        Assertions.assertEquals(Color.ROYALBLUE,colorScale.getColors().get(1));
    }

    @Test
    void setOpacity() {
        Assertions.assertThrows(Exception.class, () -> {colorScale.setOpacity(new Color(1.0,1.0,1.0,1.0),-1.0);});
        Assertions.assertThrows(Exception.class, () -> {colorScale.setOpacity(new Color(1.0,1.0,1.0,1.0),2.0);});
        Assertions.assertEquals(new Color(0.5,0.5,0.5,0.5),colorScale.setOpacity(new Color(1.0,1.0,1.0,1.0),0.5));
    }


}