package met.se211.Reservation.repository;

import met.se211.Reservation.model.Reservation;
import met.se211.other.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByGuest(Guest guest);
}
