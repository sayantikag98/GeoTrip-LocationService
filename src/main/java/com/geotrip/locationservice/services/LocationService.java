package com.geotrip.locationservice.services;


import com.geotrip.locationservice.dtos.DriverLocationDto;
import com.geotrip.locationservice.dtos.FindNearbyDriverRequestDto;
import com.geotrip.locationservice.dtos.SaveDriverLocationRequestDto;

import java.util.List;


public interface LocationService {
    void saveDriverLocation(SaveDriverLocationRequestDto saveDriverLocationRequestDto);

    List<DriverLocationDto> findNearbyDrivers(FindNearbyDriverRequestDto findNearbyDriverRequestDto);
}
