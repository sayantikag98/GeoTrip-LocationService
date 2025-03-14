package com.geotrip.locationservice.services;

import com.geotrip.exceptionhandler.AppException;
import com.geotrip.locationservice.dtos.DriverLocationDto;
import com.geotrip.locationservice.dtos.ExactLocationDto;
import com.geotrip.locationservice.dtos.FindNearbyDriverRequestDto;
import com.geotrip.locationservice.dtos.SaveDriverLocationRequestDto;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class RedisGeoSpatialLocationServiceImpl implements LocationService {

    private static final String REDIS_KEY = "drivers";
    private static final String REDIS_KEY_TTL = "drivers-ttl";
    private final GeoOperations<String, String> geoOps;
    private final HashOperations<String, String, String> hashOps;

    public RedisGeoSpatialLocationServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.geoOps = redisTemplate.opsForGeo();
        this.hashOps = redisTemplate.opsForHash();
    }

    @Override
    public Boolean saveDriverLocation(SaveDriverLocationRequestDto saveDriverLocationRequestDto) {
        String driverId = saveDriverLocationRequestDto.getDriverId().toString();

        Point point = new Point(saveDriverLocationRequestDto.getExactLocationDto().getLongitude(),
                saveDriverLocationRequestDto.getExactLocationDto().getLatitude()
                );

        RedisGeoCommands.GeoLocation<String> geoLocation= new RedisGeoCommands.GeoLocation<>(
                driverId, point
        );

        Long added = this.geoOps.add(
                REDIS_KEY,
                geoLocation
        );

        if (added == null) {
            throw new AppException("Driver location cannot be added in Redis", HttpStatus.BAD_REQUEST);
        }

        // set TTL: 10secs
        this.hashOps.put(REDIS_KEY_TTL, driverId,  String.valueOf(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10)));
        return true;
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

        if(searchResults == null){
            throw new AppException("Nearby drivers cannot be fetched", HttpStatus.BAD_REQUEST);
        }

        List<DriverLocationDto> driverLocationDtoList = new ArrayList<>();

        for(GeoResult<RedisGeoCommands.GeoLocation<String>> result: searchResults.getContent()){
            String driverId = result.getContent().getName();
            String expiryString = hashOps.get(REDIS_KEY_TTL, driverId);
            if(expiryString != null){
                long expiry = Long.parseLong(expiryString);
                if(expiry > System.currentTimeMillis()){
                    driverLocationDtoList.add(
                            DriverLocationDto.builder()
                                    .driverId(UUID.fromString(driverId))
                                    .exactLocationDto(
                                            ExactLocationDto.builder()
                                                    .longitude(result.getContent().getPoint().getX())
                                                    .latitude(result.getContent().getPoint().getY())
                                                    .build()
                                    )
                                    .distance(result.getDistance().getValue())
                                    .build()
                    );
                }
                else{
                    removeExpiredDrivers(driverId);
                }
            }
        }
        return driverLocationDtoList;
    }


    //to clean up redis data store every 6 hours at minute 0
    @Scheduled(cron = "0 0 */6 * * *")
    public void cleanExpiredDrivers() {
        Map<String, String> ttlMap =  hashOps.entries(REDIS_KEY_TTL);

        for(var entry: ttlMap.entrySet()){
            String driverId = entry.getKey();
            long expiry = Long.parseLong(entry.getValue());

            if(expiry < System.currentTimeMillis()){
                removeExpiredDrivers(driverId);
            }
        }
    }

    private void removeExpiredDrivers(String driverId) {
        geoOps.remove(REDIS_KEY, driverId);
        hashOps.delete(REDIS_KEY_TTL, driverId);
    }
}
