package com.asap.server.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
@Builder
public class TimeSlotDto {
    private String time;
    private List<String> userNames;
    private int colorLevel;
}
