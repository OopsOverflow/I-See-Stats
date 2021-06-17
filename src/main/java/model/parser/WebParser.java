package model.parser;

import model.geo.Region;
import model.species.Species;
import model.species.SpeciesData;
import org.json.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Parses a JSON Object from the URL of a Web API
 */
public class WebParser extends JasonParser {

    private String apiUrl;
    private HttpClient client;

    public WebParser(String url) {
        apiUrl = url;
        
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public void setApiUrl(String url) {
        apiUrl = url;
    }

    /**
     * Reads a JSON file from a webpage
     * @return parsed JSON file
     * @throws TimeoutException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public JSONObject readJsonFromUri(URI uri) throws InterruptedException, ExecutionException, TimeoutException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        String json = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body).get(10, TimeUnit.SECONDS);

        return new JSONObject(json);
    }

	@Override
	public SpeciesData load(ParserSettings settings) throws ParserException {
		StringBuilder builder = new StringBuilder(apiUrl);
		builder.append("/occurrence/grid/");
		builder.append(settings.precision);
		builder.append("?scientificname=");
		builder.append(settings.species.scientificName);
		
		URI uri;
		try {
			uri = new URI(builder.toString());
		} catch (URISyntaxException e) {
			throw new ParserException("invalid uri: " + builder.toString(), e);
		}
		JSONObject json;
		try {
			json = readJsonFromUri(uri);
		} catch (Exception e) {
			throw new ParserException("failed to load JSON from uri: " + builder.toString(), e);
		}
		
		ArrayList<Region> regions = loadRegionsFromJSON(json);
		
		SpeciesData data = new SpeciesData(
				settings.precision,
				settings.species,
				settings.startDate,
				regions);

		return data;
	}

	@Override
	public ArrayList<String> querySpeciesNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Species> querySpeciesByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}
