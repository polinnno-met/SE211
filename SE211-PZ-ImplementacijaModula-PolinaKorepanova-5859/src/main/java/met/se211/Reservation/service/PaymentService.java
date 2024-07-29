package met.se211.Reservation.service;

import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.PaymentRepository;
import met.se211.Reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * Processes the payment for a given reservation and updates the reservation status to "Paid".
     * @param reservationId ID of the reservation
     * @param method Payment method
     */
    public void processPayment(Long reservationId, String method) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + reservationId));

        double amount = calculateAmount(reservation);
        System.out.println(amount);

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setPaymentMethod(method);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        reservation.setStatus("Paid");
        reservationRepository.save(reservation);
    }

    /**
     * Calculates the total amount for a reservation based on the room price and the number of days.
     * @param reservation The reservation for which the amount is calculated
     * @return The total amount for the reservation
     */
    public double calculateAmount(Reservation reservation) {
        long days = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate()) + 1; // Include both ends
        return days * reservation.getRoom().getPrice();
    }

    /**
     * Finds a payment by the reservation ID.
     * @param id ID of the reservation
     * @return The payment associated with the given reservation ID
     */
    public Payment findByReservationId(Long id) {
        return paymentRepository.findByReservationId(id);
    }

    public void returnFunds(Payment payment) {
    }
}
