package met.se211.Reservation.service;

import static org.junit.jupiter.api.Assertions.*;
import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.PaymentRepository;
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

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Hotel hotel = new Hotel("Hotel Test", "Location Test", 5.0f);
        hotelRepository.save(hotel);

        Room room = new Room(hotel, "101", "single", 100.0f);
        roomRepository.save(room);

        Guest guest = new Guest("Tester Testing", "tester@test.com", "password", 1);
        userRepository.save(guest);

        reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckInDate(LocalDateTime.now());
        reservation.setCheckOutDate(LocalDateTime.now().plusDays(1));
        reservation.setStatus("Active");

        reservationRepository.save(reservation);
    }

    @Test
    void testProcessPayment() {
        paymentService.processPayment(reservation.getId(), "Credit Card");

        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElse(null);
        assertNotNull(updatedReservation);
        assertEquals("Paid", updatedReservation.getStatus());

        Payment payment = paymentRepository.findByReservationId(reservation.getId());
        assertNotNull(payment);
        assertEquals("Credit Card", payment.getPaymentMethod());
    }

    @Test
    void testCalculateAmount() {
        double amount = paymentService.calculateAmount(reservation);
        assertEquals(100.0, amount);
    }

    @Test
    void testFindByReservationId() {
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setPaymentMethod("Credit Card");
        payment.setAmount(100.0);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        Payment foundPayment = paymentService.findByReservationId(reservation.getId());
        assertNotNull(foundPayment);
        assertEquals(payment.getAmount(), foundPayment.getAmount());
    }
}
