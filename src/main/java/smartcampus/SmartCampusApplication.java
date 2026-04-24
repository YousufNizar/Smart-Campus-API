package smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import smartcampus.exception.mapper.LinkedResourceNotFoundExceptionMapper;
import smartcampus.exception.mapper.RoomNotEmptyExceptionMapper;
import smartcampus.exception.mapper.SensorUnavailableExceptionMapper;
import smartcampus.exception.mapper.ThrowableExceptionMapper;
import smartcampus.exception.mapper.JsonMappingExceptionMapper;
import smartcampus.filter.ApiLoggingFilter;
import smartcampus.resource.DiscoveryResource;
import smartcampus.resource.SensorResource;
import smartcampus.resource.SensorRoomResource;

/**
 * Public API is at {@code /api/v1}
 * {@code web.xml} maps Jersey to {@code /*}; this path is the JAX-RS application base.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(DiscoveryResource.class);
        classes.add(SensorRoomResource.class);
        classes.add(SensorResource.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(ThrowableExceptionMapper.class);
        classes.add(ApiLoggingFilter.class);
        classes.add(JsonMappingExceptionMapper.class);
        return classes;
    }
}
