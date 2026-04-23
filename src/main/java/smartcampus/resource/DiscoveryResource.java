package smartcampus.resource;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> discover(@Context UriInfo uriInfo) {
        String apiBase = uriInfo.getBaseUri().toString();
        if (apiBase.endsWith("/")) {
            apiBase = apiBase.substring(0, apiBase.length() - 1);
        }
        Map<String, Object> links = new HashMap<>();
        links.put("rooms", apiBase + "/rooms");
        links.put("sensors", apiBase + "/sensors");

        Map<String, Object> response = new HashMap<>();
        response.put("name", "Smart Campus Sensor & Room Management API");
        response.put("version", "v1");
        response.put("contact", "smartcampus-api@westminster.ac.uk");
        response.put("resources", links);
        return response;
    }
}
