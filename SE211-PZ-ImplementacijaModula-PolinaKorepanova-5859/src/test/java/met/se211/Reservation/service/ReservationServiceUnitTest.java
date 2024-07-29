package met.se211.Reservation.service;

import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.ReservationRepository;
import met.se211.other.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReservationServiceUnitTest {

    @Autowired
    private ReservationService reservationService;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private ReservationRepository reservationRepository;

    @Test
    public void testFindRoomById() {
        Room room = new Room(new Hotel("Hotel Test", "Location Test", 4.0f), "101", "Single", 100.0f);
        Mockito.when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Room foundRoom = reservationService.findRoomById(1L);
        assertEquals(room, foundRoom);
    }

    @Test
    public void testFindGuestById() {
        Guest guest = new Guest("Tester Testing", "tester@test.com", "password", 1);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(guest));

        Guest foundGuest = reservationService.findGuestById(1L);
        assertEquals(guest, foundGuest);
    }

    @Test
    public void testCreateReservation() {
        Room room = new Room(new Hotel("Hotel Test", "Location Test", 5.0f), "101", "Single", 100.0f);
        Guest guest = new Guest("Tester Testing", "tester@test.com", "password", 1);
        LocalDateTime checkInDate = LocalDateTime.of(2024, 7, 24, 12, 0);
        LocalDateTime checkOutDate = LocalDateTime.of(2024, 7, 26, 12, 0);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);

        Mockito.when(reservationRepository.save(Mockito.any(Reservation.class))).thenReturn(reservation);

        Reservation createdReservation = reservationService.createReservation(room, guest, checkInDate, checkOutDate);
        assertEquals(room, createdReservation.getRoom());
        assertEquals(guest, createdReservation.getGuest());
        assertEquals(checkInDate, createdReservation.getCheckInDate());
        assertEquals(checkOutDate, createdReservation.getCheckOutDate());
    }

    @Test
    public void testFindReservationsByGuestId() {
        Guest guest = new Guest("Tester Testing", "tester@test.com", "password", 1);
        Reservation reservation = new Reservation();
        reservation.setGuest(guest);

        List<Reservation> reservations = List.of(reservation);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(guest));
        Mockito.when(reservationRepository.findByGuest(guest)).thenReturn(reservations);

        List<Reservation> foundReservations = reservationService.findReservationsByGuestId(1L);
        assertEquals(reservations, foundReservations);
    }

    @Test
    public void testCancel() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus("Active");

        Payment payment = new Payment();
        payment.setReservation(reservation);

        Mockito.when(reservationRepository.save(Mockito.any(Reservation.class))).thenReturn(reservation);
        Mockito.when(paymentService.findByReservationId(1L)).thenReturn(payment);

        reservationService.cancel(reservation);
        assertEquals("Canceled", reservation.getStatus());
        Mockito.verify(paymentService, Mockito.times(1)).returnFunds(payment);
        Mockito.verify(reservationRepository, Mockito.times(1)).save(reservation);
    }
}
