package met.se211.Reservation.service;

import static org.junit.jupiter.api.Assertions.*;
import met.se211.Reservation.model.Payment;
import met.se211.Reservation.model.Reservation;
import met.se211.Reservation.repository.PaymentRepository;
import met.se211.Reservation.repository.ReservationRepository;
import met.se211.other.Room;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceUnitTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testProcessPayment() {
        Room room = new Room();
        room.setPrice(100.0f);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setRoom(room);
        reservation.setCheckInDate(LocalDateTime.now());
        reservation.setCheckOutDate(LocalDateTime.now().plusDays(1));
        reservation.setStatus("Active");

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        paymentService.processPayment(1L, "Credit Card");

        assertEquals("Paid", reservation.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void testCalculateAmount() {
        Room room = new Room();
        room.setPrice(100.0f);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setCheckInDate(LocalDateTime.now());
        reservation.setCheckOutDate(LocalDateTime.now().plusDays(2));

        double amount = paymentService.calculateAmount(reservation);

        assertEquals(300.0, amount);
    }

    @Test
    void testFindByReservationId() {
        Payment payment = new Payment();
        payment.setId(1L);

        when(paymentRepository.findByReservationId(1L)).thenReturn(payment);

        Payment foundPayment = paymentService.findByReservationId(1L);

        assertNotNull(foundPayment);
        assertEquals(1L, foundPayment.getId());
    }
}