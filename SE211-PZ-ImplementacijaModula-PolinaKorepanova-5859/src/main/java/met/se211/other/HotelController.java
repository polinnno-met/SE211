package met.se211.other;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HotelController {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping("/hotels/{hotelId}")
    public String getHotelDetails(@PathVariable Long hotelId, Model model) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new IllegalArgumentException("Invalid hotel Id:" + hotelId));
        model.addAttribute("hotel", hotel);
        model.addAttribute("rooms", roomRepository.findByHotelId(hotelId));
        return "hotel-details";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/hotels/1";
    }
}
