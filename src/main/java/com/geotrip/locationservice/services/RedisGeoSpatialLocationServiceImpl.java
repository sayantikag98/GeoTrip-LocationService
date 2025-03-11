package com.geotrip.locationservice.services;

import com.geotrip.locationservice.dtos.DriverLocationDto;
import com.geotrip.locationservice.dtos.ExactLocationDto;
import com.geotrip.locationservice.dtos.FindNearbyDriverRequestDto;
import com.geotrip.locationservice.dtos.SaveDriverLocationRequestDto;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class RedisGeoSpatialLocationServiceImpl implements LocationService {

    private static final String REDIS_KEY = "drivers";
    private final GeoOperations<String, String> geoOps;

    public RedisGeoSpatialLocationServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.geoOps = redisTemplate.opsForGeo();
    }

    @Override
    public void saveDriverLocation(SaveDriverLocationRequestDto saveDriverLocationRequestDto) {
        this.geoOps.add(
                REDIS_KEY,
                new RedisGeoCommands.GeoLocation<>(
                        saveDriverLocationRequestDto.getDriverId().toString(),
                        new Point(
                                saveDriverLocationRequestDto.getExactLocationDto().getLongitude(),
                                saveDriverLocationRequestDto.getExactLocationDto().getLatitude()
                        )
                )
        );
    }

    @Override
    public List<DriverLocationDto> findNearbyDrivers(FindNearbyDriverRequestDto findNearbyDriverRequestDto) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> searchResults = this.geoOps.radius(
                REDIS_KEY,
                new Circle(
                        new Point(
                                findNearbyDriverRequestDto.getExactLocationDto().getLongitude(),
                                findNearbyDriverRequestDto.getExactLocationDto().getLatitude()
                        ),
                        new Distance(findNearbyDriverRequestDto.getRadius(), Metrics.KILOMETERS)
                ),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance().sortAscending().limit(findNearbyDriverRequestDto.getLimit())
        );
        assert searchResults != null;
        return searchResults.getContent().stream().map(result -> new DriverLocationDto(
                UUID.fromString(result.getContent().getName()),
                ExactLocationDto.builder()
                        .longitude(result.getContent().getPoint().getX())
                        .latitude(result.getContent().getPoint().getY())
                        .build(),
                result.getDistance().getValue()
        )).collect(Collectors.toList());
    }
}
