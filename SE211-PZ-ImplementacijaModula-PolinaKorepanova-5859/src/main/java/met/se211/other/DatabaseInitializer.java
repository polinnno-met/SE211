package met.se211.other;
import met.se211.Reservation.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import met.se211.Reservation.repository.ReservationRepository;

import java.time.LocalDateTime;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (hotelRepository.count() == 0) {
            Hotel hotel1 = new Hotel("Grand Budapest Hotel", "Zubrowka", 4.8f);

            hotelRepository.save(hotel1);

            Room room201 = new Room(hotel1, "201", "double", 120.0f);
            roomRepository.save(new Room(hotel1, "101", "single", 105.0f));
            roomRepository.save(new Room(hotel1, "102", "double", 110.0f));
            roomRepository.save(room201);
            roomRepository.save(new Room(hotel1, "202", "double", 200.0f));

            Guest guest1 = new Guest("M. Gustave", "mgustave@grandbudapest.com", "1234", 60);
            userRepository.save(guest1);

            Reservation reservation = new Reservation();
            reservation.setRoom(room201);
            reservation.setGuest(guest1);
            reservation.setCheckInDate(LocalDateTime.of(2024, 7, 25, 14, 0));
            reservation.setCheckOutDate(LocalDateTime.of(2024, 7, 30, 11, 0));

            reservationRepository.save(reservation);
        }
    }
}
