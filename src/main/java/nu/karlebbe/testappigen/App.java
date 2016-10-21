package nu.karlebbe.testappigen;

import static spark.Spark.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import spark.ModelAndView;
import spark.Spark;



/**
 * Hello world!
 *
 */
public class App 
{
	private static String PREFIX_API = "/api/v1";
	
    public static void main( String[] args ){
		Gson gson = new Gson();

		Spark.staticFileLocation("/public");
		Spark.port(8080);
    
        System.out.println( "Hello World!" );
        
       get(PREFIX_API + "/travels", (request, response) ->{
    	   String retStr = "";
    	   String parameterDeparture = request.queryParams("departure");
    	   String parameterArrival = request.queryParams("arrival");
    	   String parameterDate = request.queryParams("date");
    	   System.out.println("departure = " + parameterDeparture);
    	   System.out.println("arrival = " + parameterArrival);
    	   System.out.println("date = " + parameterDate);
    	   
    	   HttpResponse<JsonNode> responseLocations;
    	   HttpResponse<JsonNode> responseTravels;
    	   JSONArray responseArray = null;
   		   JsonNode responseJson = null;
   		   String departureName = null;
   		   String departureID = null;
   		   String arrivalName = null;
   		   String arrivalID = null;
		
   		   JSONArray locationsArray = null;
   		   try { // Avgångsplats
   			   responseLocations = getLocations(parameterDeparture);  	//Här hämtas data från resrobot.se
 
   			   responseJson = responseLocations.getBody();
   			   locationsArray = responseJson.getArray();
   			   if(locationsArray.length() > 0){							//Då finns det något i svaret.
   				   departureName = (String) locationsArray.getJSONObject(0).getJSONArray("StopLocation").getJSONObject(0).get("name");
   				   departureID = (String) locationsArray.getJSONObject(0).getJSONArray("StopLocation").getJSONObject(0).get("id");
   			   }

   			   System.out.println("departureName = " + departureName);
   			   System.out.println("departureID = " + departureID);
			
   			   // Ankomstplats
   			   responseLocations = getLocations(parameterArrival);
   			   responseJson = responseLocations.getBody();

   			   locationsArray = responseJson.getArray();
   			   if(locationsArray.length() > 0){				//Då finns det något i svaret.
   				   arrivalName = (String) locationsArray.getJSONObject(0).getJSONArray("StopLocation").getJSONObject(0).get("name");
   				   arrivalID = (String) locationsArray.getJSONObject(0).getJSONArray("StopLocation").getJSONObject(0).get("id");
   			   }

   			   System.out.println("arrivalName = " + arrivalName);
   			   System.out.println("arrivalID = " + arrivalID);

   			   // Datum för resan
   			   responseTravels = getTravels(departureID, arrivalID, parameterDate);
   			   responseJson = responseTravels.getBody();
   			   responseArray = responseJson.getArray();
   			   
   			   System.out.println("responseJson, resor = " + responseArray.toString());
   			//Unirest.shutdown();
//   		} catch (UnirestException e) {
   			// TODO Auto-generated catch block
//   			e.printStackTrace();
   		//} catch (IOException e) {
   			// TODO Auto-generated catch block
   		//	e.printStackTrace();
   		   }catch (Exception e){
   			   System.out.println(e);
   		   }

    	   
   		   response.header("Access-Control-Allow-Origin", "*");
   		   response.status(200);
    	   
//   		   response.body(responseJson.toString()); 
   		   response.body(responseArray.toString());
   		   response.type("application/json");
    	   return response.body(); // Skicka tillbaka svaret

       });
       
		get("/", (request, response) -> {
				response.header("Access-Control-Allow-Origin", "*");
				
				response.status(200);
				return response.body(); // Skicka tillbaka svaret
		});
        
    }
    
    /**
     * Hämtar orter från resrobo.se
     * @param location, ungefärligt eller exakt namn på orten.
     * @return en JsonNod innehållande de orter och dess id som matchar 
     * @throws UnirestException
     */
    private static HttpResponse<JsonNode> getLocations(String location) throws UnirestException{
    	HttpResponse<JsonNode> response = Unirest.get("https://api.resrobot.se/v2/location.name")
		.header("accept", "application/json")
		.queryString("key", "80e9e6df-a88b-4ba7-ab9b-43f73b0bdfe7")
		.queryString("input", location)
		.queryString("format", "json")
		.asJson();    	
    	
    	return response;
    }

    /**
     * Hämtar resor utifrån medsänd avgånsplats, destination och datum.
     * @param departure, avgångs ID från resrobot.se
     * @param arrival, ankomst ID från resrobot.se
     * @param date, datum för resan.
     * @return, en JsonNode innehållande de resor som matchar.
     * @throws UnirestException
     */
    private static HttpResponse<JsonNode> getTravels(String departure, String arrival, String date) throws UnirestException{
    	HttpResponse<JsonNode> response = Unirest.get("https://api.resrobot.se/v2/trip")
		.header("accept", "application/json")
		.queryString("key", "80e9e6df-a88b-4ba7-ab9b-43f73b0bdfe7")
		.queryString("originId", departure)
		.queryString("destId", arrival)
		.queryString("date", date)
		.queryString("format", "json")
		.asJson();    	
    	
    	return response;
    }

    
}
