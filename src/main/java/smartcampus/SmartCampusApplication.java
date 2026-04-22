package smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import smartcampus.exception.mapper.LinkedResourceNotFoundExceptionMapper;
import smartcampus.exception.mapper.RoomNotEmptyExceptionMapper;
import smartcampus.exception.mapper.SensorUnavailableExceptionMapper;
import smartcampus.exception.mapper.ThrowableExceptionMapper;
import smartcampus.filter.ApiLoggingFilter;
import smartcampus.resource.DiscoveryResource;
import smartcampus.resource.SensorResource;
import smartcampus.resource.SensorRoomResource;

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
        return classes;
    }
}
