package met.se211.Reservation.controller;

import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.ReservationRepository;
import met.se211.Reservation.service.PaymentService;
import met.se211.Reservation.service.ReservationService;
import met.se211.other.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReservationController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentService paymentService;

    /**
     * Displays the reservation form for a specific room.
     * @param roomId ID of the room
     * @param model Model to pass data to the view
     * @return The name of the view for the reservation form
     */
    @GetMapping("/reserve/{roomId}")
    public String showReservationForm(@PathVariable Long roomId, Model model) {
        Guest guest = (Guest) userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("Invalid guest Id"));
        Room room = reservationService.findRoomById(roomId);

        model.addAttribute("room", room);
        model.addAttribute("guest", guest);
        return "reservation-form";
    }

    /**
     * Creates a new reservation based on the provided data and returns a confirmation.
     * @param roomId ID of the room
     * @param guestId ID of the guest
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param redirectAttributes Attributes to pass messages between controllers
     * @param model Model to pass data to the view
     * @return The name of the view for reservation confirmation
     */
    @PostMapping("/reserve")
    public String makeReservation(@RequestParam Long roomId,
                                  @RequestParam Long guestId,
                                  @RequestParam LocalDateTime checkInDate,
                                  @RequestParam LocalDateTime checkOutDate,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (checkInDate.isBefore(LocalDateTime.now()) || checkOutDate.isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reservation dates cannot be in the past.");
            return "redirect:/reservations";
        }
        if (checkInDate.isAfter(checkOutDate)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Check-in date must be before check-out date.");
            return "redirect:/reservations";
        }
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + roomId));
        Guest guest = (Guest) userRepository.findById(guestId).orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + guestId));

        Reservation reservation = reservationService.createReservation(room, guest, checkInDate, checkOutDate);

        reservationRepository.save(reservation);

        model.addAttribute("reservation", reservation);
        return "reservation-confirmation";
    }

    /**
     * Retrieves all reservations for a specific guest.
     * @param model Model to pass data to the view
     * @return The name of the view for displaying reservations
     */
    @GetMapping("/reservations")
    public String getReservationsForGuest(Model model) {
        List<Reservation> reservations = reservationService.findReservationsByGuestId(1L);
        model.addAttribute("reservations", reservations);
        return "reservations";
    }

    /**
     * Displays the form to edit a reservation based on its ID.
     * @param id ID of the reservation
     * @param model Model to pass data to the view
     * @param redirectAttributes Attributes to pass messages between controllers
     * @return The name of the view for the edit reservation form
     */
    @GetMapping("/edit-reservation/{id}")
    public String editReservation(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + id));


        if ("Paid".equals(reservation.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reservation has been paid and cannot be changed.");
            return "redirect:/reservations";
        }

        model.addAttribute("reservation", reservation);
        return "edit-reservation-form";
    }

    /**
     * Updates an existing reservation based on the provided data.
     * @param id ID of the reservation
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param redirectAttributes Attributes to pass messages between controllers
     * @param model Model to pass data to the view
     * @return Redirects to the list of reservations
     */
    @PostMapping("/update-reservation")
    public String updateReservation(@RequestParam Long id,
                                    @RequestParam LocalDateTime checkInDate,
                                    @RequestParam LocalDateTime checkOutDate,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (checkInDate.isBefore(LocalDateTime.now()) || checkOutDate.isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reservation dates cannot be in the past.");
            return "redirect:/reservations";
        }
        if (checkInDate.isAfter(checkOutDate)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Check-in date must be before check-out date.");
            return "redirect:/reservations";
        }
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + id));
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        reservationRepository.save(reservation);
        return "redirect:/reservations";
    }

    /**
     * Cancels a reservation based on its ID.
     * @param id ID of the reservation
     * @return Redirects to the list of reservations
     */
    @GetMapping("/cancel-reservation/{id}")
    public String cancelReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + id));
        reservationService.cancel(reservation);
        return "redirect:/reservations";
    }

    /**
     * Displays the payment form for a specific reservation.
     * @param id ID of the reservation
     * @param model Model to pass data to the view
     * @return The name of the view for the payment form
     */
    @GetMapping("/pay-reservation/{id}")
    public String showPaymentForm(@PathVariable Long id, Model model) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + id));
        double amount = paymentService.calculateAmount(reservation);

        model.addAttribute("reservation", reservation);
        model.addAttribute("payment", new Payment());
        model.addAttribute("amount", amount);

        return "payment-form";
    }

    /**
     * Processes the payment for a specific reservation.
     * @param reservationId ID of the reservation
     * @param method Payment method
     * @param model Model to pass data to the view
     * @return Redirects to the list of reservations
     */
    @PostMapping("/pay-reservation")
    public String payReservation(@RequestParam Long reservationId,
                                 @RequestParam String method,
                                 Model model) {
        paymentService.processPayment(reservationId, method);
        return "redirect:/reservations";
    }
}
