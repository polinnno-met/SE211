package met.se211.Reservation.service;

import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.PaymentRepository;
import met.se211.Reservation.repository.ReservationRepository;
import met.se211.other.Guest;
import met.se211.other.Room;
import met.se211.other.RoomRepository;
import met.se211.other.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationRepository reservationRepository;

    public Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + roomId));
    }

    public Guest findGuestById(Long guestId) {
        return (Guest) userRepository.findById(guestId).orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + guestId));
    }

    /**
     * Creates a new reservation for a specified room and guest, with check-in and check-out dates.
     * @param room The room being reserved
     * @param guest The guest making the reservation
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @return The created reservation
     */
    public Reservation createReservation(Room room, Guest guest, LocalDateTime checkInDate, LocalDateTime checkOutDate) {

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setGuest(guest);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);

        return reservationRepository.save(reservation);
    }

    /**
     * Finds all reservations made by a specified guest.
     * @param guestId ID of the guest
     * @return A list of reservations made by the guest
     */
    public List<Reservation> findReservationsByGuestId(Long guestId) {
        Guest guest = findGuestById(guestId);
        return reservationRepository.findByGuest(guest);
    }

    /**
     * Cancels a specified reservation and returns any associated funds.
     * @param reservation The reservation to be canceled
     */
    public void cancel(Reservation reservation) {
        reservation.setStatus("Canceled");
        reservationRepository.save(reservation);
        Payment payment = paymentService.findByReservationId(reservation.getId());
        paymentService.returnFunds(payment);
    }
}
