package met.se211.Reservation.service;
import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.ReservationRepository;
import met.se211.other.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Room room;
    private Guest guest;

    @BeforeEach
    void setUp() {
        Hotel hotel = new Hotel("Hotel Test", "Location Test", 5.0f);
        hotelRepository.save(hotel);

        room = new Room(hotel, "101", "single", 100.0f);
        roomRepository.save(room);

        guest = new Guest("Tester Testing", "tester@test.com", "password", 1);
        userRepository.save(guest);
    }

    @Test
    void testFindRoomById() {
        Room foundRoom = reservationService.findRoomById(room.getId());
        assertNotNull(foundRoom);
        assertEquals(room.getId(), foundRoom.getId());
    }

    @Test
    void testFindGuestById() {
        Guest foundGuest = reservationService.findGuestById(guest.getId());
        assertNotNull(foundGuest);
        assertEquals(guest.getId(), foundGuest.getId());
    }

    @Test
    void testCreateReservation() {
        LocalDateTime checkInDate = LocalDateTime.now();
        LocalDateTime checkOutDate = LocalDateTime.now().plusDays(1);
        Reservation reservation = reservationService.createReservation(room, guest, checkInDate, checkOutDate);

        assertNotNull(reservation);
        assertEquals(room.getId(), reservation.getRoom().getId());
        assertEquals(guest.getId(), reservation.getGuest().getId());
    }

    @Test
    void testFindReservationsByGuestId() {
        LocalDateTime checkInDate = LocalDateTime.now();
        LocalDateTime checkOutDate = LocalDateTime.now().plusDays(1);
        Reservation reservation = reservationService.createReservation(room, guest, checkInDate, checkOutDate);

        List<Reservation> reservations = reservationService.findReservationsByGuestId(guest.getId());

        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
        assertEquals(reservation.getId(), reservations.get(0).getId());
    }

    @Test
    void testCancel() {
        LocalDateTime checkInDate = LocalDateTime.now();
        LocalDateTime checkOutDate = LocalDateTime.now().plusDays(1);
        Reservation reservation = reservationService.createReservation(room, guest, checkInDate, checkOutDate);

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setPaymentMethod("Credit Card");
        payment.setAmount(100.0);
        payment.setPaymentDate(LocalDateTime.now());
        paymentService.processPayment(reservation.getId(), "Credit Card");

        reservationService.cancel(reservation);

        Reservation canceledReservation = reservationRepository.findById(reservation.getId()).orElse(null);
        assertNotNull(canceledReservation);
        assertEquals("Canceled", canceledReservation.getStatus());
    }
}
