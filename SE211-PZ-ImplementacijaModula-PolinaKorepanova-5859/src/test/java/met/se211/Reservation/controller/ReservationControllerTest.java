package met.se211.Reservation.controller;

import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.ReservationRepository;
import met.se211.Reservation.service.PaymentService;
import met.se211.Reservation.service.ReservationService;
import met.se211.other.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private PaymentService paymentService;

    private Guest guest;
    private Room room;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        guest = new Guest("Tester Testing", "tester@test.com", "password", 1);
        room = new Room(new Hotel("Hotel Test", "Location Test", 5.0f), "101", "single", 100.0f);
        reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckInDate(LocalDateTime.now());
        reservation.setCheckOutDate(LocalDateTime.now().plusDays(1));
        reservation.setStatus("Active");
    }

    @Test
    void showReservationForm() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(reservationService.findRoomById(1L)).thenReturn(room);

        mockMvc.perform(get("/reserve/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation-form"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("guest"));
    }

    @Test
    void makeReservation() throws Exception {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(reservationService.createReservation(any(Room.class), any(Guest.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(reservation);

        mockMvc.perform(post("/reserve")
                        .param("roomId", "1")
                        .param("guestId", "1")
                        .param("checkInDate", LocalDateTime.now().plusDays(1).toString())
                        .param("checkOutDate", LocalDateTime.now().plusDays(2).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation-confirmation"))
                .andExpect(model().attributeExists("reservation"));
    }

    @Test
    void getReservationsForGuest() throws Exception {
        when(reservationService.findReservationsByGuestId(1L)).thenReturn(Arrays.asList(reservation));

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations"))
                .andExpect(model().attributeExists("reservations"));
    }

    @Test
    void editReservation() throws Exception {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        mockMvc.perform(get("/edit-reservation/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-reservation-form"))
                .andExpect(model().attributeExists("reservation"));
    }

    @Test
    void updateReservation() throws Exception {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        mockMvc.perform(post("/update-reservation")
                        .param("id", "1")
                        .param("checkInDate", LocalDateTime.now().plusDays(1).toString())
                        .param("checkOutDate", LocalDateTime.now().plusDays(2).toString()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/reservations"));
    }

    @Test
    void cancelReservation() throws Exception {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        Mockito.doNothing().when(reservationService).cancel(any(Reservation.class));

        mockMvc.perform(get("/cancel-reservation/1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/reservations"));
    }

    @Test
    void showPaymentForm() throws Exception {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentService.calculateAmount(reservation)).thenReturn(200.0);

        mockMvc.perform(get("/pay-reservation/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-form"))
                .andExpect(model().attributeExists("reservation"))
                .andExpect(model().attributeExists("payment"))
                .andExpect(model().attributeExists("amount"));
    }

    @Test
    void payReservation() throws Exception {
        Mockito.doNothing().when(paymentService).processPayment(1L, "Credit Card");

        mockMvc.perform(post("/pay-reservation")
                        .param("reservationId", "1")
                        .param("method", "Credit Card"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/reservations"));
    }
}
