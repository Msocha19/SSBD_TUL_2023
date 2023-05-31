package pl.lodz.p.it.ssbd2023.ssbd05.utils.converters;

import pl.lodz.p.it.ssbd2023.ssbd05.entities.mow.Place;
import pl.lodz.p.it.ssbd2023.ssbd05.mow.cdi.endpoint.dto.response.PlaceDto;

import java.util.List;

public class PlaceDtoConverter {

    public static PlaceDto createPlaceDtoFromPlace(Place place) {
        return new PlaceDto(
            place.getId(),
            place.getPlaceNumber(),
            AccountDtoConverter.createAddressDtoFromAddress(place.getBuilding().getAddress())
        );
    }

    public static List<PlaceDto> createPlaceDtoList(List<Place> places) {
        return places.stream()
            .map(PlaceDtoConverter::createPlaceDtoFromPlace)
            .toList();
    }
}
