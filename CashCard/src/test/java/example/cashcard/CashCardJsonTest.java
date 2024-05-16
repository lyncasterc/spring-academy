package example.cashcard;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    private CashCard[] cashCards;

    @BeforeEach
    void setup() {
        cashCards = Arrays.array(
                new CashCard(99L, 123.45, "sarah1"),
                new CashCard(100L, 1.00, "sarah1"),
                new CashCard(101L, 150.00, "sarah1")
        );
    }

    @Test
    void cashCardSerializationTest() throws IOException {
//        CashCard card = new CashCard(99L, 123.45);
        CashCard card = cashCards[0];

        // serialization - converting the object into a byte stream (json string)
        JsonContent<CashCard> content = json.write(card);

        assertThat(content).isStrictlyEqualToJson("single.json");
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
                        "amount": 123.45,
                        "owner": "sarah1"
                    }
                """;

        assertThat(json.parse(expected)).isEqualTo(new CashCard(99L, 123.45, "sarah1"));
    }

    @Test
    void cashCardListSerializationTest() throws IOException {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    @Test
    void cashCardListDeserializationTest() throws IOException {
        String expected = """
                    [
                        {"id": 99, "amount": 123.45, "owner": "sarah1"},
                        {"id": 100, "amount": 1.00, "owner": "sarah1"},
                        {"id": 101, "amount": 150.00, "owner": "sarah1"}
                    ]
                """;

        assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }
}
