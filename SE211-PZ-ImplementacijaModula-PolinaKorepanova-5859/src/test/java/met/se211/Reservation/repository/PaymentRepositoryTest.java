package met.se211.Reservation.repository;

import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.other.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class PaymentRepositoryTest {

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
    void testFindByReservationId() {
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setPaymentMethod("Credit Card");
        payment.setAmount(200.0);
        payment.setPaymentDate(LocalDateTime.now());

        paymentRepository.save(payment);

        Payment foundPayment = paymentRepository.findByReservationId(reservation.getId());
        assertNotNull(foundPayment);
        assertEquals(payment.getAmount(), foundPayment.getAmount());
    }
}
