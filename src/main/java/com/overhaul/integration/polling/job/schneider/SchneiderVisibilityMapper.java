package com.overhaul.integration.polling.job.schneider;

import com.overhaul.smarttruck.model.GeoLocationType;
import com.overhaul.smarttruck.model.ShipmentType;
import com.overhaul.smarttruck.model.SmartTruckEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * Created by branganathan on 2/12/2018.
 */
@Mapper
public interface SchneiderVisibilityMapper {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    SchneiderVisibilityMapper INSTANCE = Mappers.getMapper(SchneiderVisibilityMapper.class);

    @Mappings({
            @Mapping(target = "shipment.broker_ref", source = "orderID"),
            @Mapping(target = "shipment.status", source = "orderStatus"),
            @Mapping(target = "curposition", source = "lastReportedLocation")
    })
    SmartTruckEvent convertToSmartTruckEvent(com.overhaul.integration.polling.job.schneider.SchneiderVisibilityAPIResponse apiResponse);

    @Mappings({
            @Mapping(target = "lat", source = "lastReportedLatitude"),
            @Mapping(target = "lng", source = "lastReportedLongitude"),
            @Mapping(target = "time", source = "lastReportedDateTime", dateFormat = DATE_FORMAT)
    })
    GeoLocationType mapCurPosition(com.overhaul.integration.polling.job.schneider.LastReportedLocation lastReportedLocation);
}
