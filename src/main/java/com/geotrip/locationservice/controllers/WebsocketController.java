package com.geotrip.locationservice.controllers;


import com.geotrip.locationservice.dtos.ExactLocationDto;
import com.geotrip.locationservice.dtos.SaveDriverLocationRequestDto;
import com.geotrip.locationservice.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebsocketController {


    private final SimpMessagingTemplate messagingTemplate;
    private final LocationService locationService;


    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    @MessageMapping("/driver/location")
    public void updateDriverLocation(@Payload SaveDriverLocationRequestDto saveDriverLocationRequestDto) {
        //save the driver location in Redis
        locationService.saveDriverLocation(saveDriverLocationRequestDto);

        //broadcast the updated location to the client who have subscribed to this topic
        messagingTemplate.convertAndSend("/topic/driver-location/"+saveDriverLocationRequestDto.getDriverId().toString(),
                ExactLocationDto.builder()
                        .longitude(saveDriverLocationRequestDto.getExactLocationDto().getLongitude())
                        .latitude(saveDriverLocationRequestDto.getExactLocationDto().getLatitude())
                        .build()
        );

    }

}
