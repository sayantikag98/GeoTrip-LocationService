package com.geotrip.locationservice.controllers;


import com.geotrip.locationservice.dtos.DriverLocationDto;
import com.geotrip.locationservice.dtos.FindNearbyDriverRequestDto;
import com.geotrip.locationservice.dtos.SaveDriverLocationRequestDto;
import com.geotrip.locationservice.services.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/location/drivers")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public ResponseEntity<Boolean> saveDriverLocation(@RequestBody @Valid SaveDriverLocationRequestDto saveDriverLocationRequestDto) {
        this.locationService.saveDriverLocation(saveDriverLocationRequestDto);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/nearby")
    public ResponseEntity<List<DriverLocationDto>> getNearbyDrivers(@RequestBody @Valid FindNearbyDriverRequestDto findNearbyDriverRequestDto) {
        List<DriverLocationDto> nearbyDriversList = this.locationService.findNearbyDrivers(findNearbyDriverRequestDto);
        return ResponseEntity.ok(nearbyDriversList);
    }


}
