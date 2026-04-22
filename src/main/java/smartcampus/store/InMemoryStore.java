package smartcampus.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import smartcampus.model.Room;
import smartcampus.model.Sensor;
import smartcampus.model.SensorReading;

public final class InMemoryStore {

    private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> SENSORS = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> SENSOR_READINGS = new ConcurrentHashMap<>();

    private InMemoryStore() {
    }

    public static List<Room> getAllRooms() {
        return new ArrayList<>(ROOMS.values());
    }

    public static Room getRoom(String roomId) {
        return ROOMS.get(roomId);
    }

    public static Room saveRoom(Room room) {
        ROOMS.put(room.getId(), room);
        return room;
    }

    public static Room deleteRoom(String roomId) {
        return ROOMS.remove(roomId);
    }

    public static boolean roomExists(String roomId) {
        return ROOMS.containsKey(roomId);
    }

    public static boolean roomHasSensors(String roomId) {
        Room room = ROOMS.get(roomId);
        return room != null && room.getSensorIds() != null && !room.getSensorIds().isEmpty();
    }

    public static List<Sensor> getAllSensors() {
        return new ArrayList<>(SENSORS.values());
    }

    public static List<Sensor> getSensorsByType(String type) {
        return SENSORS.values()
                .stream()
                .filter(sensor -> sensor.getType() != null && sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public static Sensor getSensor(String sensorId) {
        return SENSORS.get(sensorId);
    }

    public static Sensor saveSensor(Sensor sensor) {
        SENSORS.put(sensor.getId(), sensor);
        Room room = ROOMS.get(sensor.getRoomId());
        if (room != null) {
            synchronized (room) {
                if (!room.getSensorIds().contains(sensor.getId())) {
                    room.getSensorIds().add(sensor.getId());
                }
            }
        }
        return sensor;
    }

    public static List<SensorReading> getSensorReadings(String sensorId) {
        return new ArrayList<>(SENSOR_READINGS.getOrDefault(sensorId, Collections.emptyList()));
    }

    public static SensorReading addSensorReading(String sensorId, SensorReading reading) {
        SensorReading readingToStore = reading;
        if (readingToStore.getId() == null || readingToStore.getId().isBlank()) {
            readingToStore.setId(UUID.randomUUID().toString());
        }
        if (readingToStore.getTimestamp() <= 0) {
            readingToStore.setTimestamp(System.currentTimeMillis());
        }

        List<SensorReading> readings = SENSOR_READINGS.computeIfAbsent(sensorId,
                key -> Collections.synchronizedList(new ArrayList<>()));
        readings.add(readingToStore);

        Sensor parent = SENSORS.get(sensorId);
        if (parent != null) {
            parent.setCurrentValue(readingToStore.getValue());
        }
        return readingToStore;
    }
}
