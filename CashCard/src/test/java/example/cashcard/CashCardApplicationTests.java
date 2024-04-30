package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACashCard_WhenDataIsSaved() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards/99", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(res.getBody());
        Number id = documentContext.read("$.id");

        assertThat(id).isNotNull();
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");

        assertThat(amount).isNotNull();
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    void shouldNotReturnACashCardWithUnknownId() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards/1000", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).isBlank();
    }


}


