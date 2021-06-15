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

public class JasonParser implements Parser {
	
	private String root;
	
	public JasonParser() {
		root = "src/resources/JSON/";
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
	public SpeciesData load(ParserSettings settings) throws ParserException {
		String text = null;
		String filename = root + settings.species.name + ".json";
				
		try(Reader reader = new FileReader(filename)) {
			BufferedReader rd = new BufferedReader(reader);
			text = readAll(rd);
		}
		catch(IOException e) {
			throw new ParserException("JSON file not found or unreadable: " + filename);
		}
		
		ArrayList<Region> regions =  loadRegionsFromJSON(new JSONObject(text));
		
		SpeciesData data = new SpeciesData(
				settings.precision, 
				settings.species, 
				settings.startDate,
				regions);
		
		return data;
	}
	
	protected ArrayList<Region> loadRegionsFromJSON(JSONObject root) throws ParserException {
		ArrayList<Region> regions = new ArrayList<Region>();
		
		try {
	 		if(!root.getString("type").equals("FeatureCollection")) {
				throw new ParserException("Incompatible JSON file");
			}
			
			JSONArray features = root.getJSONArray("features");
			
			for(Object obj : features) {
				
				if(!(obj instanceof JSONObject)) continue;
				
				JSONObject feature = (JSONObject)obj;
				if(!feature.getString("type").equals("Feature")) continue;
				JSONObject properties = feature.getJSONObject("properties");
				
				JSONObject geometry = feature.getJSONObject("geometry");
				if(!root.getString("type").equals("Polygon")) continue;
				JSONArray coordinates = geometry.getJSONArray("coordinates");
				
				int count = properties.getInt("n");
				
				// finds the area's latitude, longitude and size
				float avgLat = 0;
				float avgLon = 0;
				for(int i = 0; i < 4; i++) {
					JSONArray coords = coordinates.getJSONArray(i);
					avgLat += coords.getFloat(0);
					avgLon += coords.getFloat(1);
				}
				avgLat /= 4;
				avgLon /= 4;
				
				
				// TODO: use GeoHash.fromLatLon(avgLat, avgLon, precision) once available. 
				Region region = new Region(count, GeoHash.fromString("000"));
				regions.add(region);
			}
		}
		catch (JSONException e) {
			throw new ParserException("Malformed JSON file");
		}
		
		return regions;
	}

	@Override
	public ArrayList<String> querySpeciesNames() {
		// TODO Auto-generated method stub
		// TODO: must list the files in root directory.
		return null;
	}

	@Override
	public ArrayList<Species> querySpeciesByName(String name) {
		// TODO Auto-generated method stub
		// TODO: idk
		return null;
	}
    
    
}
