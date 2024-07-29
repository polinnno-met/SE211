package met.se211.Reservation.controller;

import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.ReservationRepository;
import met.se211.Reservation.service.PaymentService;
import met.se211.Reservation.service.ReservationService;
import met.se211.other.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ReservationControllerUnitTest {

    @InjectMocks
    private ReservationController reservationController;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationService reservationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    private Guest guest;
    private Room room;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    void showReservationForm() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(reservationService.findRoomById(1L)).thenReturn(room);

        String viewName = reservationController.showReservationForm(1L, model);

        assertEquals("reservation-form", viewName);
        verify(model, times(1)).addAttribute(eq("room"), eq(room));
        verify(model, times(1)).addAttribute(eq("guest"), eq(guest));
    }

    @Test
    void makeReservation() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(reservationService.createReservation(any(Room.class), any(Guest.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(reservation);

        String viewName = reservationController.makeReservation(1L, 1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), redirectAttributes, model);

        assertEquals("reservation-confirmation", viewName);
        verify(model, times(1)).addAttribute(eq("reservation"), eq(reservation));
    }

    @Test
    void getReservationsForGuest() {
        when(reservationService.findReservationsByGuestId(1L)).thenReturn(Arrays.asList(reservation));

        String viewName = reservationController.getReservationsForGuest(model);

        assertEquals("reservations", viewName);
        verify(model, times(1)).addAttribute(eq("reservations"), anyList());
    }

    @Test
    void editReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        String viewName = reservationController.editReservation(1L, model, redirectAttributes);

        assertEquals("edit-reservation-form", viewName);
        verify(model, times(1)).addAttribute(eq("reservation"), eq(reservation));
    }

    @Test
    void updateReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        String viewName = reservationController.updateReservation(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), redirectAttributes, model);

        assertEquals("redirect:/reservations", viewName);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void cancelReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationService).cancel(any(Reservation.class));

        String viewName = reservationController.cancelReservation(1L);

        assertEquals("redirect:/reservations", viewName);
        verify(reservationService, times(1)).cancel(any(Reservation.class));
    }

    @Test
    void showPaymentForm() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentService.calculateAmount(reservation)).thenReturn(200.0);

        String viewName = reservationController.showPaymentForm(1L, model);

        assertEquals("payment-form", viewName);
        verify(model, times(1)).addAttribute(eq("reservation"), eq(reservation));
        verify(model, times(1)).addAttribute(eq("payment"), any(Payment.class));
        verify(model, times(1)).addAttribute(eq("amount"), eq(200.0));
    }

    @Test
    void payReservation() {
        doNothing().when(paymentService).processPayment(1L, "Credit Card");

        String viewName = reservationController.payReservation(1L, "Credit Card", model);

        assertEquals("redirect:/reservations", viewName);
        verify(paymentService, times(1)).processPayment(1L, "Credit Card");
    }
}
