package model.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import model.geo.GeoHash;
import model.geo.Region;
import model.species.Species;
import model.species.SpeciesData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.geometry.Point2D;


public class JasonParser extends Parser {

	private String root;

	public JasonParser() {
		root = "src/main/resources/JSON/";
	}

	public JasonParser(String root){
		this.root = root;
	}

	public void setRootDirectory(String dirname){
		this.root = dirname;
	}

	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;

		while((cp = rd.read()) != -1) {
			sb.append((char)cp);
		}

		return sb.toString();
	}

	@Override
	public ParserQuery<SpeciesData> load(ParserSettings settings) {
		ParserQuery<SpeciesData> res = new ParserQuery<SpeciesData>();
		String text = null;
		String filename = root + settings.species.scientificName + ".json";

		ArrayList<Region> regions;
		try(Reader reader = new FileReader(filename)) {
			BufferedReader rd = new BufferedReader(reader);
			text = readAll(rd);
			regions = loadRegionsFromJSON(new JSONObject(text));
		}
		catch(IOException e) {
			ParserException parserException = new ParserException(ParserException.Type.FILE_NOT_FOUND, e);
			return res.fireError(parserException);
		}
		catch(JSONException e) {
			ParserException parserException = new ParserException(ParserException.Type.JSON_PARSE_ERROR, e);
			return res.fireError(parserException);
		}
		catch (ParserException e) {
			return res.fireError(e);
		}

		SpeciesData data = new SpeciesData(settings, regions);
		return res.fireSuccess(data);
	}

	protected ArrayList<Species> loadSpeciesFromJSON(JSONObject root) throws ParserException {
		ArrayList<Species> res = new ArrayList<Species>();

		try {
			JSONArray results = root.getJSONArray("results");

			for(Object obj : results) {

				if(!(obj instanceof JSONObject)) continue;

				JSONObject jsonSpecies = (JSONObject)obj;

				String scientificName = jsonSpecies.getString("scientificName");
				Species species = new Species(scientificName);

				try { species.order = jsonSpecies.getString("order"); } catch(JSONException e) {}
				try { species.superclass = jsonSpecies.getString("class"); } catch(JSONException e) {} // TODO: what is "superclass" supposed to refer to ?

				res.add(species);
			}
		}
		catch (JSONException e) {
			throw new ParserException(ParserException.Type.JSON_MALFORMED);
		}

		return res;
	}

	protected ArrayList<Region> loadRegionsFromJSON(JSONObject root) throws ParserException {
		ArrayList<Region> regions = new ArrayList<Region>();

		try {
			if(!root.getString("type").equals("FeatureCollection")) {
				throw new ParserException(ParserException.Type.JSON_MALFORMED);
			}

			JSONArray features = root.getJSONArray("features");

			for(Object obj : features) {

				if(!(obj instanceof JSONObject)) continue;

				JSONObject feature = (JSONObject)obj;
				if(!feature.getString("type").equals("Feature")) continue;
				JSONObject properties = feature.getJSONObject("properties");

				JSONObject geometry = feature.getJSONObject("geometry");
				if(!geometry.getString("type").equals("Polygon")) continue;
				JSONArray coordinates = geometry.getJSONArray("coordinates");

				int count = properties.getInt("n");

				// finds the area bounds
				double latMin = 90.0, lonMin = 180.0, latMax = -90.0, lonMax = -180.0;
				for(int i = 0; i < 4; i++) {
					JSONArray coords = coordinates.getJSONArray(0).getJSONArray(i);
					latMin = Math.min(latMin, coords.getDouble(1));
					latMax = Math.max(latMax, coords.getDouble(1));
					lonMin = Math.min(lonMin, coords.getDouble(0));
					lonMax = Math.max(lonMax, coords.getDouble(0));
				}

				GeoHash hash = GeoHash.fromArea(new Point2D(latMin, lonMin), new Point2D(latMax, lonMax));
				Region region = new Region(count, hash);
				regions.add(region);
			}
		}
		catch (JSONException e) {
			throw new ParserException(ParserException.Type.JSON_MALFORMED);
		}

		return regions;
	}

	protected ArrayList<Species> loadAutocompleteFromJSON(JSONArray root) throws ParserException {
		ArrayList<Species> res = new ArrayList<Species>();


		try {
			for(Object obj : root) {
				if(!(obj instanceof JSONObject)) continue;
				JSONObject jsonSpecies = (JSONObject)obj;

				String scientificName = jsonSpecies.getString("scientificName");
				Species species = new Species(scientificName);
				res.add(species);

				try { species.order = jsonSpecies.getString("order"); } catch(JSONException e) {}
				try { species.superclass = jsonSpecies.getString("class"); } catch(JSONException e) {} // TODO: what is "superclass" supposed to refer to ?
				try { species.recordedBy = jsonSpecies.getString("institutionCode"); } catch(JSONException e) {}
			}
		}
		catch (JSONException e) {
			throw new ParserException(ParserException.Type.JSON_MALFORMED);
		}

		return res;
	}

	public ParserQuery<ArrayList<Species>> querySpeciesAtGeoHash(GeoHash geohash, int maxCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParserQuery<ArrayList<Species>> autocompleteSpecies(String partial) {
		// TODO Auto-generated method stub
		return null;
	}


}
