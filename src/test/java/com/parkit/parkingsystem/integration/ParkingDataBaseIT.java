package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.commons.beanutils.ResultSetDynaClass;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static Connection con;
    private static PreparedStatement ps;
    private static ResultSet rs;


    @Mock
    private static InputReaderUtil inputReaderUtil;


    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();

    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
        con = dataBaseTestConfig.getConnection();
    }

    @AfterEach
        private void tearDownPerTest() throws Exception {
        dataBaseTestConfig.closeResultSet(rs);
        dataBaseTestConfig.closePreparedStatement(ps);
    }

    @AfterAll
    private static void tearDown(){
        dataBaseTestConfig.closeConnection(con);

    }

    @Test
    public void testParkingACar() throws SQLException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        ps = con.prepareStatement("SELECT t.VEHICLE_REG_NUMBER, p.AVAILABLE FROM ticket t, parking p WHERE t.PARKING_NUMBER = p.PARKING_NUMBER AND t.VEHICLE_REG_NUMBER=? ORDER BY t.ID DESC");
        ps.setString(1,"ABCDEF");
        ResultSet rs = ps.executeQuery();
        rs.next();

        assertEquals(rs.getString(1),"ABCDEF");
        assertEquals(rs.getInt(2),0);


        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
    }

    @Test
    public void testParkingLotExit() throws SQLException {
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        ps = con.prepareStatement("SELECT PRICE, OUT_TIME FROM ticket WHERE VEHICLE_REG_NUMBER=? ORDER BY ID DESC");
        ps.setString(1, "ABCDEF");
        ResultSet rs = ps.executeQuery();
        rs.next();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        assertEquals(rs.getInt(1),0);
        assertEquals(rs.getString(2), timeStamp);
        //TODO: check that the fare generated and out time are populated correctly in the database
    }

}
