package example.cashcard;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestId}")
    private ResponseEntity<CashCard> getCashCardById(@PathVariable Long requestId) {
        // Optional is a container for an object which may or may not be null
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestId);

        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        }

        return ResponseEntity.notFound().build();
    }
}
