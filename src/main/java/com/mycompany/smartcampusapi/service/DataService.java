/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi.service;

/**
 *
 * @author fafft
 */


import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.model.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataService {

    private static final DataService INSTANCE = new DataService();

    private final Map<String, Room> rooms = new ConcurrentHashMap<String, Room>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<String, Sensor>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<String, List<SensorReading>>();

    private DataService() {
        Room room1 = new Room("LIB-301", "Library Quiet Study", 60);
        Room room2 = new Room("LAB-101", "Computer Lab 101", 40);

        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);

        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 25.5, "LIB-301");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "MAINTENANCE", 450.0, "LAB-101");

        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);

        room1.addSensor(sensor1.getId());
        room2.addSensor(sensor2.getId());

        sensorReadings.put(sensor1.getId(), new ArrayList<SensorReading>());
        sensorReadings.put(sensor2.getId(), new ArrayList<SensorReading>());
    }

    public static DataService getInstance() {
        return INSTANCE;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }

    public List<Sensor> getSensorsByType(String type) {
        return sensors.values()
                .stream()
                .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<SensorReading>()).add(reading);

        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }

        return reading;
    }
}