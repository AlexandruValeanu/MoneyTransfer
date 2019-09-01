import com.jayway.restassured.RestAssured;
import io.vertx.core.json.JsonObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

public class MoneyTransferIT {

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("http.port", 8080);
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        RestAssured.reset();
    }

    @Test
    public void testGetAccounts(){
        Assert.assertEquals("[]", get("/accounts/" ).thenReturn().asString());

        String jsonStringAlex = given()
                .body("{\"user\":\"alex\", \"currency\":\"USD\", \"balance\":100}")
                .request()
                .post("/accounts")
                .thenReturn().asString();

        String jsonStringBen = given()
                .body("{\"user\":\"ben\", \"currency\":\"CAD\", \"balance\":15}")
                .request()
                .post("/accounts")
                .thenReturn().asString();

        String jsonStringAccounts = get("/accounts/" ).thenReturn().asString();

        Assert.assertTrue(jsonStringAccounts.contains(jsonStringAlex));
        Assert.assertTrue(jsonStringAccounts.contains(jsonStringBen));

        String alexID = new JsonObject(jsonStringAlex).getString("id");

        delete("/accounts/" + alexID).then()
                .assertThat()
                .statusCode(204);

        String benID = new JsonObject(jsonStringBen).getString("id");

        delete("/accounts/" + benID).then()
                .assertThat()
                .statusCode(204);
    }

    @Test
    public void testGetMissingAccount(){
        get("/accounts/" + "0").then()
                .assertThat()
                .statusCode(404);

        get("/accounts/" + "thomas").then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void testAddInvalidAccount() {
        // test empty user
        given()
                .body("{\"user\":\"\", \"currency\":\"USD\", \"balance\":100}")
                .request()
                .post("/accounts")
                .then()
                .statusCode(400);

        // test null user
        given()
                .body("{\"user\":, \"currency\":\"USD\", \"balance\":100}")
                .request()
                .post("/accounts")
                .then()
                .statusCode(400);

        // test invalid currency
        given()
                .body("{\"user\":\"alex\", \"currency\":\"alex\", \"balance\":100}")
                .request()
                .post("/accounts")
                .then()
                .statusCode(400);

        // test invalid balance
        given()
                .body("{\"user\":\"alex\", \"currency\":\"USD\", \"balance\":-1}")
                .request()
                .post("/accounts")
                .then()
                .statusCode(400);

        // test missing user field
        given()
                .body("{\"currency\":\"USD\", \"balance\":100}")
                .request()
                .post("/accounts")
                .then()
                .statusCode(400);

        // test missing currency field
        given()
                .body("{\"user\":\"alex\", \"balance\":100}")
                .request()
                .post("/accounts")
                .then()
                .statusCode(400);

        // test missing balance field
        given()
                .body("{\"user\":\"alex\", \"currency\":\"USD\"")
                .request()
                .post("/accounts")
                .then()
                .statusCode(400);
    }

    @Test
    public void testAddAndUpdateAccount() {
        String jsonString = given()
                .body("{\"user\":\"alex\", \"currency\":\"USD\", \"balance\":100}")
                .request()
                .post("/accounts")
                .thenReturn().asString();

        JsonObject jsonObject = new JsonObject(jsonString);
        String id = jsonObject.getString("id");

        given()
                .body("{\"balance\":100.2345}")
                .request()
                .put("/accounts/" + id)
                .then()
                .statusCode(200);

        get("/accounts/" + id).then()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("user", equalTo("alex"))
                .body("currency", equalTo("USD"))
                .body("balance", equalTo(100.2345f));

        // test invalid balance
        given()
                .body("{\"balance\":thomas}")
                .request()
                .put("/accounts/" + id)
                .then()
                .statusCode(400);

        // test null balance
        given()
                .body("{\"balance\":}")
                .request()
                .put("/accounts/" + id)
                .then()
                .statusCode(400);

        // test no balance at all
        given()
                .body("{}")
                .request()
                .put("/accounts/" + id)
                .then()
                .statusCode(400);

        // update (valid) balance + currency (to be ignored)
        given()
                .body("{\"currency\":\"CAD\", \"balance\":3.14}")
                .request()
                .put("/accounts/" + id)
                .then()
                .statusCode(200);

        get("/accounts/" + id).then()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("user", equalTo("alex"))
                .body("currency", equalTo("USD"))
                .body("balance", equalTo(3.14f));

        delete("/accounts/" + id).then()
                .assertThat()
                .statusCode(204);
    }

    @Test
    public void testUpdateMissingAccount(){
        given()
                .body("{\"balance\":100}")
                .request()
                .put("/accounts/" + "0")
                .then()
                .statusCode(404);

        given()
                .body("{\"balance\":100}")
                .request()
                .put("/accounts/" + "thomas")
                .then()
                .statusCode(404);
    }

    @Test
    public void testAddAndDeleteAccount() {
        String jsonString = given()
            .body("{\"user\":\"alex\", \"currency\":\"USD\", \"balance\":100}")
            .request()
            .post("/accounts")
            .thenReturn().asString();

        JsonObject jsonObject = new JsonObject(jsonString);
        String id = jsonObject.getString("id");

        get("/accounts/" + id).then()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("user", equalTo("alex"))
                .body("currency", equalTo("USD"))
                .body("balance", equalTo(100));

        delete("/accounts/" + id).then()
                .assertThat()
                .statusCode(204);

        get("/accounts/" + id).then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void testDeleteMissingAccount(){
        delete("/accounts/" + "0").then()
                .assertThat()
                .statusCode(404);

        delete("/accounts/" + "thomas").then()
                .assertThat()
                .statusCode(404);
    }

    private Map.Entry<String, String> addTwoAccounts(){
        String jsonStringAlex = given()
                .body("{\"user\":\"alex\", \"currency\":\"USD\", \"balance\":10}")
                .request()
                .post("/accounts")
                .thenReturn().asString();

        String jsonStringBen = given()
                .body("{\"user\":\"ben\", \"currency\":\"USD\", \"balance\":0}")
                .request()
                .post("/accounts")
                .thenReturn().asString();

        String alexID = new JsonObject(jsonStringAlex).getString("id");
        String benID = new JsonObject(jsonStringBen).getString("id");

        return new AbstractMap.SimpleEntry<>(alexID, benID);
    }

    @Test
    public void testGetTransfers(){
        Map.Entry<String, String> pair = addTwoAccounts();

        String alexID = pair.getKey();
        String benID = pair.getValue();

        String jsonStringTransfer1 = given()
                .body("{\"source-id\":\"" + alexID + "\", \"dest-id\":\"" + benID + "\", \"amount\":5}")
                .request()
                .post("/transfers")
                .thenReturn().asString();

        String transfer1ID = new JsonObject(jsonStringTransfer1).getString("id");

        String jsonStringTransfer2 = given()
                .body("{\"source-id\":\"" + alexID + "\", \"dest-id\":\"" + benID + "\", \"amount\":4}")
                .request()
                .post("/transfers")
                .thenReturn().asString();

        String transfer2ID = new JsonObject(jsonStringTransfer2).getString("id");

        String jsonStringTransfers = get("/transfers/").thenReturn().asString();

        Assert.assertTrue(jsonStringTransfers.contains(transfer1ID));
        Assert.assertTrue(jsonStringTransfers.contains(transfer2ID));

        delete("/accounts/" + alexID).then()
                .assertThat()
                .statusCode(204);

        delete("/accounts/" + benID).then()
                .assertThat()
                .statusCode(204);
    }

    @Test
    public void testGetTransfer(){
        Map.Entry<String, String> pair = addTwoAccounts();

        String alexID = pair.getKey();
        String benID = pair.getValue();

        String jsonStringTransfer = given()
                .body("{\"source-id\":\"" + alexID + "\", \"dest-id\":\"" + benID + "\", \"amount\":5}")
                .request()
                .post("/transfers")
                .thenReturn().asString();

        String transferID = new JsonObject(jsonStringTransfer).getString("id");

        jsonStringTransfer = get("/transfers/" + transferID).thenReturn().asString();

        Assert.assertEquals(transferID, new JsonObject(jsonStringTransfer).getString("id"));

        delete("/accounts/" + alexID).then()
                .assertThat()
                .statusCode(204);

        delete("/accounts/" + benID).then()
                .assertThat()
                .statusCode(204);
    }

    @Test
    public void testInvalidTransfer(){
        Map.Entry<String, String> pair = addTwoAccounts();

        String alexID = pair.getKey();
        String benID = pair.getValue();

        // test same account transfer
        given()
                .body("{\"source-id\":\"" + benID + "\", \"dest-id\":\"" + benID + "\", \"amount\":5}")
                .request()
                .post("/transfers")
                .then()
                .assertThat()
                .statusCode(400);

        // test invalid amount
        given()
                .body("{\"source-id\":\"" + alexID + "\", \"dest-id\":\"" + benID + "\", \"amount\":0}")
                .request()
                .post("/transfers")
                .then()
                .assertThat()
                .statusCode(400);

        // test invalid account transfer
        given()
                .body("{\"source-id\":\"" + "0" + "\", \"dest-id\":\"" + benID + "\", \"amount\":5}")
                .request()
                .post("/transfers")
                .then()
                .assertThat()
                .statusCode(400);

        // test missing source account transfer
        given()
                .body("{\"dest-id\":\"" + benID + "\", \"amount\":5}")
                .request()
                .post("/transfers")
                .then()
                .assertThat()
                .statusCode(400);

        // test missing destination account transfer
        given()
                .body("{\"source-id\":\"" + benID + "\", \"amount\":5}")
                .request()
                .post("/transfers")
                .then()
                .assertThat()
                .statusCode(400);

        // test missing amount transfer
        given()
                .body("{\"source-id\":\"" + alexID + "\", \"dest-id\":\"" + benID + "\"}")
                .request()
                .post("/transfers")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
