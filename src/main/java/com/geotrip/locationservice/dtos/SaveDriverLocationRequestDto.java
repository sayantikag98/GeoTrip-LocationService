package com.geotrip.locationservice.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveDriverLocationRequestDto {

    @NotNull(message = "Driver Id must not be null")
    private UUID driverId;

    @NotNull(message = "Exact Location must not be null")
    private ExactLocationDto exactLocationDto;

}
