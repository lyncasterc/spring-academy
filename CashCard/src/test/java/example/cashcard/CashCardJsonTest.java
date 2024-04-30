package example.cashcard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CashCardJsonTest {
    @Autowired
    private JacksonTester<CashCard> json;

    @Test
    void cashCardSerializationTest() throws IOException {
        CashCard card = new CashCard(99L, 123.45);

        // serialization - converting the object into a byte stream (json string)
        JsonContent<CashCard> content = json.write(card);

        assertThat(content).isStrictlyEqualToJson("expected.json");

        assertThat(content).hasJsonPathNumberValue("@.id");

        assertThat(content).extractingJsonPathNumberValue("@.id").isEqualTo(99);

        assertThat(content).hasJsonPathNumberValue("@.amount");

        assertThat(content).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(123.45);
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        String expected = """
            {
                "id": 99,
                "amount": 123.45
            }
        """;

        assertThat(json.parse(expected)).isEqualTo(new CashCard(99L, 123.45));
    }

}
