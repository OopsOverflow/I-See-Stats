package model.parser;

import model.species.Species;
import model.species.SpeciesData;
import org.json.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Parses a JSON Object from the URL of a Web API
 */
public class WebParser implements Parser {

    URL apiUrl;
    JSONObject data;

    public WebParser(String url){
        setApiUrl(url);
        data = readJsonFromUrl();
    }

    public void setApiUrl(String url){
        try {
            apiUrl = new URL(url);
        } catch (MalformedURLException e) {
            System.err.println("Invalid API URL");
            e.printStackTrace();
        }
    }

    /**
     * Reads a JSON file from a webpage
     * @return parsed JSON file
     */
    public JSONObject readJsonFromUrl(){
        String json = "";
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.apiUrl.toString()))
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

        // TODO: 14/06/2021 Change filename for later or find better solution
        try (FileWriter file = new FileWriter("/JSON/output.json")) {
            file.write(data.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("JSON file created.");

        return new JasonParser("src/resources/JSON/output.json")
                .load(settings);
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
