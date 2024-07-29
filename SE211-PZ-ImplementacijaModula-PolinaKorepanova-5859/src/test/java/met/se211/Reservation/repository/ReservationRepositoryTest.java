package met.se211.Reservation.repository;

import met.se211.Reservation.model.Reservation;
import met.se211.other.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Guest guest;

    @BeforeEach
    void setUp() {
        Hotel hotel = new Hotel("Hotel Test", "Location Test", 5.0f);
        hotelRepository.save(hotel);

        Room room = new Room(hotel, "101", "single", 100.0f);
        roomRepository.save(room);

        guest = new Guest("Tester Testing", "tester@test.com", "password", 1);
        userRepository.save(guest);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckInDate(LocalDateTime.now());
        reservation.setCheckOutDate(LocalDateTime.now().plusDays(1));
        reservation.setStatus("Active");

        reservationRepository.save(reservation);
    }

    @Test
    void testFindByGuest() {
        List<Reservation> reservations = reservationRepository.findByGuest(guest);
        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
    }
}
