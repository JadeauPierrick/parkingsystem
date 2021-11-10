package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;


import java.util.concurrent.TimeUnit;

public class FareCalculatorService {

    private TicketDAO ticketDAO;
    
    public FareCalculatorService (TicketDAO ticketDAO){
        this.ticketDAO = ticketDAO;
    }



    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }


        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        long duration = outHour - inHour;

        double minutes = TimeUnit.MILLISECONDS.toMinutes(duration);

        double endTime = (minutes / 60) - 0.5; // -0.5 First thirty minutes free

        if (endTime < 0) {
            endTime = 0;
        }


        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(endTime * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(endTime * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
        boolean recurrentUser = ticketDAO.fivePercent(ticket.getVehicleRegNumber());
        if (recurrentUser) {
            ticket.setPrice(ticket.getPrice() * 0.95);
        }
    }
}