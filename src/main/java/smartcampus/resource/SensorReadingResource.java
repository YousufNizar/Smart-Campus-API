package smartcampus.resource;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import smartcampus.exception.SensorUnavailableException;
import smartcampus.model.Sensor;
import smartcampus.model.SensorReading;
import smartcampus.store.InMemoryStore;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        return InMemoryStore.getSensorReadings(sensorId);
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = InMemoryStore.getSensor(sensorId);
        if (sensor != null && "MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is in MAINTENANCE mode and cannot accept readings.");
        }
        SensorReading saved = InMemoryStore.addSensorReading(sensorId, reading);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }
}
