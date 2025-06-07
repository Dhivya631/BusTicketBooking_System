package com.application.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private String name;
    private String email;
    private String busNumber;
    private String origin;
    private String destination;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
    private int seatNumber;
    private LocalDate bookingDate;
    private String paymentStatus;
    private double fare;

    public BookingDTO(Long id, String busNumber, String origin, String destination, LocalTime arrivalTime, LocalTime departureTime, int seatNumber, LocalDate bookingDate, String paymentStatus, double fare) {
        this.id=id;
        this.busNumber=busNumber;
        this.origin=origin;
        this.destination=destination;
        this.arrivalTime= LocalDateTime.from(arrivalTime);
        this.departureTime= LocalDateTime.from(departureTime);
        this.seatNumber=seatNumber;
        this.bookingDate=bookingDate;
        this.paymentStatus=paymentStatus;
        this.fare=fare;
    }

    public BookingDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }
}