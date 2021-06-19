package model.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import model.geo.Region;
import model.species.Species;
import model.species.SpeciesData;


/**
 * Parses a JSON Object from the URL of a Web API
 */
public class WebParser extends JasonParser {

    private String apiUrl;
    private HttpClient client;
    DateTimeFormatter formatter; // YYYY-MM-DD

    public WebParser(String url) {
        apiUrl = url;
        
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        
        formatter = DateTimeFormatter.ISO_LOCAL_DATE;	
    }

    public void setApiUrl(String url) {
        apiUrl = url;
    }

    private CompletableFuture<String> makeRequest(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        		.thenApply(HttpResponse::body);
    }

	@Override
	public ParserQuery<SpeciesData> load(ParserSettings settings) {
		ParserQuery<SpeciesData> res = new ParserQuery<SpeciesData>();
		StringBuilder builder = new StringBuilder(apiUrl);
		builder.append("/occurrence/grid/");
		builder.append(settings.precision);
		builder.append("?scientificname=");
		builder.append(settings.species.scientificName);
		
		if(settings.startDate != null && settings.endDate != null) {
			builder.append("&startdate=");
			builder.append(settings.startDate.format(formatter));
			builder.append("&enddate=");
			builder.append(settings.endDate.format(formatter));
		}
		
		URI uri;
		
		try {
			uri = new URI(builder.toString());
		} catch (URISyntaxException e) {
			ParserException parserException = new ParserException(ParserException.Type.FILE_NOT_FOUND, e);
			return res.fireError(parserException);
		}
		
		makeRequest(uri)
		.orTimeout(10, TimeUnit.SECONDS)
		.whenCompleteAsync( (String body, Throwable err) -> {
			
			if(err != null) {
				ParserException parserException = new ParserException(ParserException.Type.NETWORK_ERROR, err);
				res.fireError(parserException);
			}
			
			else {
				JSONObject json = new JSONObject(body);
				
				ArrayList<Region> regions;
				try {
					regions = loadRegionsFromJSON(json);
				} catch (ParserException e) {
					res.fireError(e);
					return;
				}
				
				SpeciesData data = new SpeciesData(
						settings.precision,
						settings.species,
						settings.startDate,
						regions);
				
				res.fireSuccess(data);
			}
		});
		
		return res;
	}
	
    @Override
    public ParserQuery<ArrayList<Species>> autocompleteSpecies(String partial) {
    	ParserQuery<ArrayList<Species>> res = new ParserQuery<ArrayList<Species>>();
		StringBuilder builder = new StringBuilder(apiUrl);
		builder.append("/taxon/complete/verbose/");
		builder.append(partial);
		
		URI uri;
		
		try {
			uri = new URI(builder.toString());
		} catch (URISyntaxException e) {
			ParserException parserException = new ParserException(ParserException.Type.FILE_NOT_FOUND, e);
			return res.fireError(parserException);
		}
		
		makeRequest(uri)
		.orTimeout(10, TimeUnit.SECONDS)
		.whenCompleteAsync( (String body, Throwable err) -> {
			
			if(err != null) {
				ParserException parserException = new ParserException(ParserException.Type.NETWORK_ERROR, err);
				res.fireError(parserException);
			}
			
			else {
				JSONArray json = new JSONArray(body);
				
				ArrayList<Species> species;
				try {
					species = loadAutocompleteFromJSON(json);
				} catch (ParserException e) {
					res.fireError(e);
					return;
				}
				
				res.fireSuccess(species);
			}
		});
		
		return res;
    }


	@Override
	public ParserQuery<ArrayList<String>> querySpeciesNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParserQuery<Species> querySpeciesByScientificName(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}
