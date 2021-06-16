package model.geo;

import java.util.BitSet;

import org.json.JSONArray;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

/**
 * A GeoHash manipulates globe coordinates in a user-friendly manner.
 * @see http://geohash.org
 */

public class GeoHash {
	private int precision;
	private BitSet hash;

	private static final double latOffset = 0.2;
	private static final double lonOffset = 2.8;
	private static final char[] conversionTable = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
			'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};

	private GeoHash(int precision) {
		this.precision = precision;
		this.hash = new BitSet(precision * 5);
	}

	/**
	 * Creates a GeoHash from a string representation in base 32ghs.
	 * @param str - the string hash
	 * @return a new GeoHash
	 * @throws RuntimeException if the string is not a correct hash
	 * @see https://en.wikipedia.org/wiki/Geohash#Textual_representation
	 */
	public static GeoHash fromString(String str) {
		GeoHash hash = new GeoHash(str.length());
		boolean found = false;

		for (int i = 0; i < str.length(); i++) {
			char digit = str.charAt(i);
			found = false;
			for(byte num = 0; num < conversionTable.length && !found; num++) {
				if(conversionTable[num] == digit) {
					hash.setHashDigit(str.length() - 1 - i, num);
					found = true;
				}
			}
			if(!found) {
				throw new RuntimeException("invalid geohash character: " + digit);
			}
		}

		return hash;
	}

	/**
	 * Creates a GeoHash from a latitude / longitude and a precision.
	 * @param lat - the latitude
	 * @param lon - the longitude
	 * @param precision - the hash precision
	 * @return a new GeoHash
	 */
	public static GeoHash fromLatLon(double lat, double lon, int precision) {
		GeoHash hash = new GeoHash(precision);

		double div = 1.0;

		lat += 90.0;
		lon += 180.0;

		int bits = 5 * precision;

		for(int i = bits - 1; i >= 0; i -= 2) {

			double lonThresold = 180.0 / div;
			boolean lonBit = lon >= lonThresold;
			if(lonBit) lon -= lonThresold;
			hash.hash.set(i - 0, lonBit);

			if(i - 1 >= 0)  {
				double latThresold = 90.0 / div;
				boolean latBit = lat >= latThresold;
				if(latBit) lat -= latThresold;
				hash.hash.set(i - 1, latBit);
			}

			div *= 2.0;
		}
		return hash;
	}

	/**
	 * Creates an (approximate) GeoHash two latitude / longitude pairs delimiting a rectangle.
	 * This method tries to generate a GeoHash that covers the area by guessing the precision. The resulting area
	 * will be bigger than the given rectangle.
	 * The two points make a rectangle boundaries, where x / y components are respectively latitude and longitude.
	 * @param pointA - the first rectangle boundary
	 * @param pointB - the second rectangle boundary
	 * @return a new GeoHash
	 */
	public static GeoHash fromArea(Point2D pointA, Point2D pointB) {
		// finds the area's latitude, longitude and size
		double avgLat = (pointA.getX() + pointB.getX()) / 2.0;
		double avgLon = (pointA.getY() + pointB.getY()) / 2.0;
		double latDist = Math.abs(pointA.getX() - pointB.getX());
		double lonDist = Math.abs(pointA.getY() - pointB.getY());

		int lonPrecision = (int)Math.floor(2.0 / 5.0 * Math.log(360.0 / lonDist) / Math.log(2));
		int latPrecision = (int)Math.floor(2.0 / 5.0 * Math.log(180.0 / latDist) / Math.log(2));
		int precision = Math.max(latPrecision, lonPrecision);

		return GeoHash.fromLatLon(avgLat, avgLon, precision);
	}


	/**
	 * Gets the GeoHash precision.
	 * The precision is the number of characters in the string representation.
	 * @return the precision
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * Gets the hash string representation, in base 32ghs.
	 * @return the hash as a String encoded in base 32ghs
	 * @see https://en.wikipedia.org/wiki/Geohash#Textual_representation
	 */
	public String getString() {
		StringBuilder builder = new StringBuilder();

		for(int i = 0; i < precision; i++) {
			int digit = getHashDigit(i);
			builder.append(conversionTable[digit]);
		}

		return builder.reverse().toString();
	}

	@Override
	public String toString() {
		return getString();
	}

	/**
	 * Gets the latitude and longitude associated to a given hash.
	 * The value returned is the center point of the area delimited by the hash.
	 * @return A Point2D containing the latitude in x and longitude in y
	 */
	public Point2D getLatLon() {
		double lat = 0;
		double lon = 0;
		int bits = precision * 5;
		double div = 1.0;

		// consume the hash bits into latitude & longitude
		for(int i = bits - 1; i >= 0; i -= 2) {
			lon += 180.0 / div * (hash.get(i - 0) ? 1 : 0);
			if(i - 1 >= 0) lat += 90.0  / div * (hash.get(i - 1) ? 1 : 0);
			div *= 2.0;
		}

		lat -= 90.0;
		lon -= 180.0;

		return new Point2D(lat, lon);
	}

	/**
	 * Utility function to get a single bit in an integer.
	 * @param bit - the bit to get, in range [0, number_of_bits[
	 * @param val - the number from which to extract a bit
	 * @return a number with value of 0 or 1
	 */
	private static int getBit(int bit, int val) {
		return (int)(val >> bit) & 1;
	}

	/**
	 * Sets a single "digit" in the hash, i.e. 1 precision, i.e. a 5 bit word.
	 * @param digit - the digit number, in range [0, precision[
	 * @param val - the value to store in that digit (a 5 bits number)
	 */
	private void setHashDigit(int digit, byte val) {
		int offset = 5 * digit;

		for(int k = 4; k >= 0; k--) {
			hash.set(k + offset, getBit(k, val) == 1 ? true : false);
		}
	}

	/**
	 * Gets a single "digit" in the hash, i.e. 1 precision, i.e. a 5 bit word.
	 * @param digit - the digit number, in range [0, precision[
	 * @return the value stored in that digit (a 5 bits number)
	 */
	private int getHashDigit(int digit) {
		int res = 0;
		int offset = 5 * digit;

		for(int k = 4; k >= 0; k--) {
			int bit = hash.get(k + offset) ? 1 : 0;
			res += bit << k;
		}

		return res;
	}

	/**
	 * Gets the 4 points of the quadrilateral delimited by the GeoHash area.
	 * The coordinates are given as if the earth was a sphere centered at the origin of radius 1.
	 * @return an array of four points, edges of the delimited area
	 */
	public Point3D[] getRectCoords() {
		Point2D latlon = getLatLon();

		int latDivisions = (int)Math.floor(precision * 5.0 / 2.0);
		int lonDivisions = (int)Math.ceil(precision * 5.0 / 2.0);
		double lonError = 360.0 / Math.pow(2, lonDivisions);
		double latError = 180.0 / Math.pow(2, latDivisions);

		Point3D[] points = {
				latLonToCoords(new Point2D( // top right
						latlon.getX() + latError / 2.0,
						latlon.getY() + lonError / 2.0)),
				latLonToCoords(new Point2D( // bottom right
						latlon.getX() - latError / 2.0,
						latlon.getY() + lonError / 2.0)),
				latLonToCoords(new Point2D( // bottom left
						latlon.getX() - latError / 2.0,
						latlon.getY() - lonError / 2.0)),
				latLonToCoords(new Point2D( // top left
						latlon.getX() + latError / 2.0,
						latlon.getY() - lonError / 2.0)),
		};

		return points;
	}

	/**
	 * Converts a latitude / longitude to 3D coordinates on the globe.
	 * The coordinates are given as if the earth was a sphere centered at the origin of radius 1.
	 * @return the coordinates of the point on the surface of the earth
	 */
	public static Point3D latLonToCoords(Point2D latlon) {
		double lat_cor = Math.toRadians(latlon.getX() + latOffset);
		double lon_cor = Math.toRadians(latlon.getY() + lonOffset);

		return new Point3D(
				-Math.sin(lon_cor) * Math.cos(lat_cor),
				-Math.sin(lat_cor),
				Math.cos(lon_cor) * Math.cos(lat_cor));
	}

	/**
	 * Converts 3D coordinates on the globe to latitude / longitude.
	 * The coordinates are given as if the earth was a sphere centered at the origin of radius 1.
	 * @return a points, where x / y components are respectively latitude and longitude
	 */
	public static Point2D coordsToLatLon(Point3D p) {
		double lat, lon;

		lat = Math.asin(-p.getY());
		lon = Math.asin(-p.getX() * Math.cos(lat));

		lat += latOffset;
		lon += lonOffset;

		if(p.getZ() < 0) {
			lon = Math.PI - lon;
		}

		return new Point2D(lat * 180.0 / Math.PI, lon * 180.0 / Math.PI);
	}

}
