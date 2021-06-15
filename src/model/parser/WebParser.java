package model.parser;

import model.species.Species;
import model.species.SpeciesData;
import org.json.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Parses a JSON Object from the URL of a Web API
 */
public class WebParser implements Parser {

    public void setApiUrl(String url){

    }

    /**
     * Reads a JSON file from a webpage
     * @param URL url to the page API
     * @return parsed JSON file
     */
    public static JSONObject readJsonFromUrl(String URL){
        String json = "";
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try{
            json = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONObject(json);
    }

	@Override
	public SpeciesData load(ParserSettings settings) throws ParserException {
		// TODO Auto-generated method stub
		return null;
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
