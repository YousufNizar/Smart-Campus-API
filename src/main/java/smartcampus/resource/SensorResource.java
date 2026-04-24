package smartcampus.resource;

import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import smartcampus.exception.LinkedResourceNotFoundException;
import smartcampus.model.Sensor;
import smartcampus.store.InMemoryStore;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type == null || type.isBlank()) {
            return InMemoryStore.getAllSensors();
        }
        return InMemoryStore.getSensorsByType(type);
    }
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = InMemoryStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        return Response.ok(sensor).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"sensor id is required\"}")
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank() || !InMemoryStore.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("The referenced roomId does not exist: " + sensor.getRoomId());
        }

        InMemoryStore.saveSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource sensorReadingSubResource(@PathParam("sensorId") String sensorId) {
        if (InMemoryStore.getSensor(sensorId) == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        return new SensorReadingResource(sensorId);
    }
}
