package model.geo;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ColorScale implements Observable {
    private ArrayList<Color> colors;
    private int minRange;
    private int maxRange;

    public enum Interpolation { LINEAR, LOGARITHMIC };
    private Interpolation interpolation;

    private ArrayList<InvalidationListener> listeners;

    public ColorScale(int minRange, int maxRange, ArrayList<Color> colors) {
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.colors = colors;
        this.interpolation = Interpolation.LINEAR;
        this.listeners = new ArrayList<InvalidationListener>();
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

    public ArrayList<Color> getColors() {
    	return colors;
    }

    /**
     * Return the color for a given value
     * @param value value of which you want the color
     * @return the color corresponding to the value
     */
    public Color getColor(int value) {
    	double val, min, max;

    	if(interpolation == Interpolation.LINEAR) {
            val = value;
            min = minRange;
            max = maxRange;
    	}
    	else {
            val = Math.log(value);
            min = Math.log(minRange);
            max = Math.log(maxRange);
    	}

        double indice = (colors.size() - 1) * (val-min) / (max-min);
        int clamped = (int)indice;
        clamped = Math.max(clamped, 0);
        clamped = Math.min(clamped, colors.size() - 1);
        return colors.get(clamped);
    }

    /**
     * Set the color range
     * @param minRange minimum of the range
     * @param maxRange maximum of the range
     * @throws RuntimeException send exeption in case of error
     */
    public void setRange(int minRange, int maxRange) {
        if(minRange > maxRange)
            throw new RuntimeException("Ranges are invalid");
        this.minRange = minRange;
        this.maxRange = maxRange;
        fireInvalidation();
    }
    
    /**
     * Get the minimum of the range
     * @return minimum of the range
     */
    public int getMinRange() {
    	return minRange;
    }
    
    /**
     * Get the maximum of the range
     * @return maximum of the range
     */
    public int getMaxRange() {
    	return maxRange;
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
        fireInvalidation();
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
     * Gets the number of colors in the scale.
     * @return count - the total number of colors
     */
    public int getColorCount() {
    	return colors.size();
    }

    public void setInterpolationType(Interpolation interpolation) {
    	this.interpolation = interpolation;
    	fireInvalidation();
    }

    public Interpolation getInterpolationType() {
    	return interpolation;
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
        fireInvalidation();
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

    /**
     * Makes a color with the given opacity.
     * linearly interpolated between 'minColor' and 'maxColor'.
     * @param opaque - the input color (opaque)
     * @param opacity - the opacity to set
     * @return the output color
     */
    public static Color setOpacity(Color opaque, double opacity) {
        if(opacity<0.0 || opacity>1.0)
            throw new RuntimeException("Opacity must be between 0 and 1");
		double red   = opacity * opaque.getRed();
		double green = opacity * opaque.getGreen();
		double blue  = opacity * opaque.getBlue();
    	return new Color(red, green, blue, opacity);
    }

	@Override
	public void addListener(InvalidationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		listeners.remove(listener);
	}
	
	private void fireInvalidation() {
		for(InvalidationListener listener : listeners) {
			listener.invalidated(this);
		}
	}
}
