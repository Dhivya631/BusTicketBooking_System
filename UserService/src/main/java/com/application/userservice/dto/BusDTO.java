package com.application.userservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class BusDTO {
    private String busNumber;
    private String busType;
    private int capacity;
    private List<BusScheduleDTO> schedules;

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<BusScheduleDTO> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<BusScheduleDTO> schedules) {
        this.schedules = schedules;
    }
}
