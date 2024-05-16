package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import javax.swing.text.Document;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACashCard_WhenDataIsSaved() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/99", String.class);

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
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/1000", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).isBlank();
    }


    @Test
//    @DirtiesContext tells Spring to start with clean state
        // bc this test can theoretically mess with other tests since it creates a new cashCard,
        // but it doesn't seem to be a problem for me right now
    void shouldCreateNewCashCard() {
        CashCard newCashCard = new CashCard(null, 250.00, null);

        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .postForEntity("/cashcards", newCashCard, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI newCashCardLocation = res.getHeaders().getLocation();

        // making a get request to confirm that the new cashcard was indeed created

        ResponseEntity<String> getRes = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity(newCashCardLocation, String.class);

        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getRes.getBody());
        Double amount = documentContext.read("$.amount");
        Number id = documentContext.read("$.id");

        assertThat(amount).isEqualTo(250.00);
        assertThat(id).isNotNull();
    }

    @Test
    void shouldNotCreateNewCashCard_WhenRequestBodyContainsId() {
        // passes in an id, which would not be a valid request
        CashCard newCashCard = new CashCard(1L, 250.00, "sarah1");
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .postForEntity("/cashcards", newCashCard, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void shouldReturnAllCashCards_WhenListIsRequested() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(res.getBody());
        int cashCardsLength = documentContext.read("$.length()");

        assertThat(cashCardsLength).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        JSONArray amounts = documentContext.read("$..amount");

        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
    }

    @Test
    void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards?page=0&size=1", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(res.getBody());
        int cashCardsLength = documentContext.read("$.length()");

        assertThat(cashCardsLength).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(res.getBody());
        double amount = documentContext.read("$[0].amount");

        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    void shouldReturnASortedPageOfCashCards_WhenGivenNoParams() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(res.getBody());
        int cashCardsLength = documentContext.read("$.length()");
        JSONArray amounts = documentContext.read("$..amount");

        assertThat(cashCardsLength).isEqualTo(3);
        assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
    }

    @Test
    void shouldReturn401_WhenUsingInvalidCredentials() {
        // no creds passed
        ResponseEntity<String> res = restTemplate
                .getForEntity("/cashcards", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // wrong creds passed
        res = restTemplate
                .withBasicAuth("sarah1", "abc122")
                .getForEntity("/cashcards", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    void shouldRejectUsersWhoAreNotCardOwners() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("hank", "qrs456")
                .getForEntity("/cashcards/99", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn403_WhenAccessingCardsTheyDoNotOwn() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/103", String.class); // steven1's cash card

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    @DirtiesContext
    void shouldUpdateExistingCard() {
        CashCard cashCardUpdate = new CashCard(null, 19.99, null);
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> res2 = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/99", String.class);

        DocumentContext documentContext = JsonPath.parse(res2.getBody());
        Double amount = documentContext.read("$.amount");
        Number id = documentContext.read("$.id");

        assertThat(id).isEqualTo(99);
        assertThat(amount).isEqualTo(19.99);
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateNonExistentCard() {
        CashCard cashCardUpdate = new CashCard(null, 19.99, null);
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/1002", HttpMethod.PUT, request, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateCashCard_WhenUserIsNotCardOwner() {
        CashCard cashCardUpdate = new CashCard(null, 19.99, null);
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/103", HttpMethod.PUT, request, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteExistingCashCard() {
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // GET request to make sure it's been deleted.

        ResponseEntity<String> res2 = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/99", String.class);

        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res2.getBody()).isBlank();
    }


    @Test
    @DirtiesContext
    void shouldNotDeleteCard_WhenUserIsNotCardOwner() {
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/103", HttpMethod.DELETE, null, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> res2 = restTemplate
                .withBasicAuth("steven1", "abc123")
                .getForEntity("/cashcards/103", String.class);


        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    @DirtiesContext
    void shouldNotDeleteNonExistentCard() {
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/100003", HttpMethod.DELETE, null, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}


