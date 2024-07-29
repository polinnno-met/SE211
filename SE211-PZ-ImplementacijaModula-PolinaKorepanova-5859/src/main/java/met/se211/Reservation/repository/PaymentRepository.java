package met.se211.Reservation.repository;

import met.se211.Reservation.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByReservationId(Long reservationId);
}
