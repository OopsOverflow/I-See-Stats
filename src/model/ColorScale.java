package model;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ColorScale {
    private ArrayList<Color> colors;
    private int minRange;
    private int maxRange;


    public ColorScale(int minRange, int maxRange, ArrayList<Color> colors) {
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.colors = colors;
    }

    /**
     * Constructs a ColorScale from min/max values, min/max colors and a color count
     * /!\ Must contain at least two colors
     * @param minRange - the minimum value corresponding to the lowest bound
     * @param maxRange - the maximum value corresponding to the highest bound
     * @param minColor - the minimum color of the range
     * @param maxColor - the maximum color of the range
     * @param count - the number of colors in the range
     * @return the color corresponding to the value
     */
    public ColorScale(int minRange, int maxRange, Color minColor, Color maxColor, int count) {
        this(minRange, maxRange, interpolateColors(minColor, maxColor, count));
    }

    /**
     * Return the color for a given value
     * @param value value of which you want the color
     * @return the color corresponding to the value
     */
    public Color getColor(int value) {
        if(value > maxRange) value = maxRange;
        if(value < minRange) value = minRange;

        int indice = (colors.size() - 1) * (value-minRange) / (maxRange-minRange);
        return colors.get(indice);
    }

    /**
     * Set the color range
     * @param minRange minimum of the range
     * @param maxRange maximum of the range
     * @throws Exception send exeption in case of error
     */
    public void setRange(int minRange,int maxRange) throws Exception {
        if(minRange>=maxRange)
            throw new Exception("Ranges are invalid");
        this.minRange=minRange;
        this.maxRange=maxRange;
    }

    /**
     * Set colors of the range
     * /!\ Must contain at least two colors
     * @param colors - colors of the range you want
     * @throws RuntimeException when less than two colors are provided
     */
    public void setColors(ArrayList<Color> colors) {
        if(colors.size() < 2)
            throw new RuntimeException("The color scale must contain at least two colors");

        this.colors = colors;
    }

    /**
     * Sets the number of colors in the scale. The scale will interpolate the
     * first and last color to generate the middle ones.
     * /!\ Must contain at least two colors
     * @param count - the total number of colors, including min and max
     * @throws RuntimeException when count is less than two
     * @see #setInterpolatedColors
     */
    public void setColorCount(int count) {
        Color first = colors.get(0);
        Color last = colors.get(colors.size() - 1);
        setInterpolatedColors(first, last, count);
    }

    /**
     * Sets the first color of the range and interpolates all the colors
     * between this color and the last.
     * @param minColor - first color of the color scale
     * @see #setInterpolatedColors
     */
    public void setMinColor(Color minColor) {
        Color last = colors.get(colors.size() - 1);
        setInterpolatedColors(minColor, last, colors.size());
    }

    /**
     * Sets the last color of the range and interpolates all the colors
     * between the first color and this one.
     * @param maxColor - last color of the color scale
     * @see #setInterpolatedColors
     */
    public void setMaxColor(Color maxColor) {
        Color first = colors.get(0);
        setInterpolatedColors(first, maxColor, colors.size());
    }

    /**
     * Sets the colors of the colorscale as a gradient of 'count' colors,
     * linearly interpolated between 'minColor' and 'maxColor'.
     * @param minColor - the first color of the range
     * @param maxColor - the last color of the range
     * @param count - the total number of colors, including min and max
     */
    public void setInterpolatedColors(Color minColor, Color maxColor, int count) {
        if(count < 2)
            throw new RuntimeException("The color scale must contain at least two colors");
        colors = interpolateColors(minColor, maxColor, count);
    }

    private static ArrayList<Color> interpolateColors(Color minColor, Color maxColor, int count) {
        ArrayList<Color> colors = new ArrayList<Color>();

        colors.add(minColor);
        for(int i = 0; i < count - 2; i++) {
            float interp = (i + 1) / (float)(count - 1);
            Color col = minColor.interpolate(maxColor, interp);
            colors.add(col);
        }
        colors.add(maxColor);
        return colors;
    }
}
