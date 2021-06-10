package sample;

import javafx.scene.paint.Color;

public class ColorScale {
    private Color colors[];
    private int minRange;
    private int maxRange;

    /**
     * Return the color for a given value
     * @param value value of which you want the color
     * @return the color corresponding to the value
     */
    public Color getColor(int value){
        if(value>maxRange)value=maxRange;
        if(value<minRange)value=minRange;

        int indice = Math.round(colors.length * (value-minRange)/(float)(maxRange-minRange));
        return colors[indice];

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
     * @param colors colors of the range you want
     */
    public void setColors(Color[] colors) {
        this.colors = colors;
    }
}
