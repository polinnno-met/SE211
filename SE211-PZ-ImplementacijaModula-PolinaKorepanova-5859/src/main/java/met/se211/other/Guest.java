package met.se211.other;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Guest")
public class Guest extends Users {
    private int loyaltyPoints;

    public Guest(String name, String email, String password, int loyaltyPoints) {
        super(name, email, password);
        this.loyaltyPoints = loyaltyPoints;
    }

    public Guest() {

    }

}
