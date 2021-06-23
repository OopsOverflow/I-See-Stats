package model.parser;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import model.geo.GeoHash;
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

    private String urlencode(String raw) throws ParserException {
    	String res;
		try {
			res = URLEncoder.encode(raw, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ParserException(ParserException.Type.NETWORK_ERROR, e);
		}

		return res;
    }

	@Override
	public ParserQuery<SpeciesData> load(ParserSettings settings) {
		ParserQuery<SpeciesData> res = new ParserQuery<SpeciesData>();
		StringBuilder builder = new StringBuilder(apiUrl);
		builder.append("/occurrence/grid/");
		builder.append(settings.precision);
        builder.append("?scientificname=");
        try {
            builder.append(urlencode(settings.species.scientificName));
        } catch (ParserException e) {
            return res.fireError(e);
        }
        if(settings.geoHash != null) {
            builder.append("&geohash=");
            builder.append(settings.geoHash.toString());
        }

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

				SpeciesData data = new SpeciesData(settings, regions);
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
		try {
			builder.append(urlencode(partial));
		} catch (ParserException e) {
			return res.fireError(e);
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
    public ParserQuery<ArrayList<Species>> querySpeciesAtGeoHash(GeoHash geohash, int maxCount) {
		ParserQuery<ArrayList<Species>> res = new ParserQuery<ArrayList<Species>>();
		StringBuilder builder = new StringBuilder(apiUrl);
		builder.append("/occurrence/?size=");
		builder.append(maxCount);
		builder.append("&geometry=");
		builder.append(geohash.getString());

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

				ArrayList<Species> species;
				try {
					species = loadSpeciesFromJSON(json);
				} catch (ParserException e) {
					res.fireError(e);
					return;
				}

				res.fireSuccess(species);
			}
		});

		return res;
    }
}
