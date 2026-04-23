/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi.config;

/**
 *
 * @author fafft
 */


import com.mycompany.smartcampusapi.filter.LoggingFilter;
import com.mycompany.smartcampusapi.mapper.GlobalExceptionMapper;
import com.mycompany.smartcampusapi.mapper.LinkedResourceNotFoundExceptionMapper;
import com.mycompany.smartcampusapi.mapper.RoomNotEmptyExceptionMapper;
import com.mycompany.smartcampusapi.mapper.SensorUnavailableExceptionMapper;
import com.mycompany.smartcampusapi.resource.DiscoveryResource;
import com.mycompany.smartcampusapi.resource.RoomResource;
import com.mycompany.smartcampusapi.resource.SensorResource;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        classes.add(LoggingFilter.class);
        return classes;
    }
}