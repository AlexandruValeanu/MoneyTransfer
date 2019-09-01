import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

public class MoneyTransferVerticle extends AbstractVerticle {

    private Map<UUID, Account> accounts = DataStore.getAccounts();

    private Map<UUID, Transfer> transfers = DataStore.getTransfers();

    private Gson gson = new Gson();

    @Override
    public void start(Future<Void> fut) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.route("/").handler(routingContext -> routingContext.response()
                .putHeader("content-type", "text/html")
                .end("<h1>Money transfer service</h1>"));

        router.get("/accounts").handler(this::getAccounts);
        router.get("/accounts/:id").handler(this::getAccount);
        router.post("/accounts").handler(this::addAccount);
        router.put("/accounts/:id").handler(this::updateAccount);
        router.delete("/accounts/:id").handler(this::deleteAccount);

        router.get("/transfers").handler(this::getTransfers);
        router.get("/transfers/:id").handler(this::getTransfer);
        router.post("/transfers").handler(this::addTransfer);

        vertx
            .createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("http.port", 8080),
                    result -> {
                        if (result.succeeded()) {
                            fut.complete();
                        } else {
                            fut.fail(result.cause());
                        }
                    }
            );
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        // TODO: add logging message in order to investigate bad requests
        response.setStatusCode(statusCode).end();
    }

    private void getAccounts(RoutingContext routingContext){
        routingContext.response()
                .putHeader("content-type", "application/json;")
                .end(gson.toJson(accounts.values()));
    }

    private void getAccount(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        HttpServerResponse response = routingContext.response();

        if (id == null) {
            sendError(400, response);
        } else {
            try {
                Account account = accounts.get(UUID.fromString(id));

                if (account == null) {
                    sendError(404, response);
                } else {
                    response.putHeader("content-type", "application/json").end(gson.toJson(account));
                }
            } catch (Exception e){
                sendError(404, response);
            }
        }
    }

    private void addAccount(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        JsonObject bodyAsJson;

        try {
            bodyAsJson = routingContext.getBodyAsJson();
        } catch (Exception e){
            sendError(400, response);
            return;
        }

        try{
            String user = bodyAsJson.getString("user");
            Currency currency = Currency.getInstance(bodyAsJson.getString("currency"));
            BigDecimal balance = new BigDecimal(String.valueOf(bodyAsJson.getValue("balance")));

            Account account = new Account(user, currency, balance);
            accounts.putIfAbsent(account.id, account);

            response.setStatusCode(201)
                    .putHeader("content-type", "application/json")
                    .end(gson.toJson(account));

        } catch (Exception e){
            sendError(400, response);
        }
    }

    private void updateAccount(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        HttpServerResponse response = routingContext.response();
        JsonObject bodyAsJson;

        try {
            bodyAsJson = routingContext.getBodyAsJson();
        } catch (Exception e){
            sendError(400, response);
            return;
        }

        if (id == null || bodyAsJson == null){
            sendError(400, response);
        } else{
            try {
                Account account = accounts.get(UUID.fromString(id));

                if (account == null) {
                    sendError(404, response);
                } else {
                    try {
                        BigDecimal newBalance = new BigDecimal(String.valueOf(bodyAsJson.getValue("balance")));
                        account.setBalance(newBalance);

                        response.putHeader("content-type", "application/json")
                                .end(gson.toJson(account));
                    } catch (Exception e){
                        sendError(400, response);
                    }
                }
            } catch (Exception e){
                sendError(404, response);
            }
        }
    }

    private void deleteAccount(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        HttpServerResponse response = routingContext.response();

        if (id == null) {
            sendError(400, response);
        } else {
            try {
                Account account = accounts.get(UUID.fromString(id));

                if (account == null) {
                    sendError(404, response);
                } else {
                    accounts.remove(account.id);
                    response.setStatusCode(204).end();
                }
            } catch (Exception e){
                sendError(404, response);
            }

        }
    }

    private void getTransfers(RoutingContext routingContext){
        routingContext.response()
                .putHeader("content-type", "application/json;")
                .end(gson.toJson(transfers.values()));
    }

    private void getTransfer(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        HttpServerResponse response = routingContext.response();

        if (id == null) {
            sendError(400, response);
        } else {
            try {
                Transfer transfer = transfers.get(UUID.fromString(id));

                if (transfer == null) {
                    sendError(404, response);
                } else {
                    response.putHeader("content-type", "application/json").end(gson.toJson(transfer));
                }
            } catch (Exception e){
                sendError(404, response);
            }
        }
    }

    private void addTransfer(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        JsonObject bodyAsJson;

        try {
            bodyAsJson = routingContext.getBodyAsJson();
        } catch (Exception e){
            sendError(400, response);
            return;
        }

        try{
            String sourceID = bodyAsJson.getString("source-id");
            String destinationID = bodyAsJson.getString("dest-id");
            BigDecimal amount = new BigDecimal(String.valueOf(bodyAsJson.getValue("amount")));

            Account sourceAccount = accounts.get(UUID.fromString(sourceID));
            Account destinationAccount = accounts.get(UUID.fromString(destinationID));

            Transfer transfer = new Transfer(sourceAccount, destinationAccount, amount);

            if (transfer.execute()){
                transfers.put(transfer.id, transfer);

                response.setStatusCode(201)
                        .putHeader("content-type", "application/json")
                        .end(gson.toJson(transfer));
            }
            else{
                sendError(400, response);
            }

        } catch (Exception e){
            sendError(400, response);
        }
    }
}
