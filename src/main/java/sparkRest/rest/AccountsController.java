package sparkRest.rest;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;
import static sparkRest.rest.AccountSummary.summary;

import java.math.BigDecimal;
import java.util.UUID;

import com.google.gson.Gson;

import spark.Request;
import spark.Response;
import spark.RouteGroup;
import sparkRest.accounting.Account;
import sparkRest.storage.AccountStorage;
import sparkRest.transfers.Transfer;
import sparkRest.transfers.TransferRequest;

public class AccountsController {

	private AccountStorage storage;
	private Gson gson = new Gson();
	private Transfer transfer;

	public AccountsController(AccountStorage storage, Transfer transfer) {
		if (null == storage)
			throw new IllegalArgumentException("storage");
		if (null == transfer)
			throw new IllegalArgumentException("transfer");

		this.storage = storage;
		this.transfer = transfer;
	}

	public RouteGroup setupRoutes() {
		return () -> {
			path("/accounts", () -> {
				before((req, res) -> {
					String path = req.pathInfo();
					if (path.endsWith("/"))
						res.redirect(path.substring(0, path.length() - 1));
				});
				post("", this::createAccount);
				path("/:accountId", () -> {
					get("", this::getAccountById, gson::toJson);
					delete("", this::deleteAccount);
					get("/state", this::getAccountState, gson::toJson);
					put("/state", this::setAccountState);
					post("/transfers", this::transfer, gson::toJson);
				});
			});
		};
	}

	public Object createAccount(Request req, Response res) {
		var request = newAccountRequest(req);

		if (null == request)
			return this.badRequest(res);

		return created(res, req.uri() + "/" + storage.newAccount(request));
	}

	public Object getAccountById(Request req, Response res) {
		var account = storage.get(req.params(":accountId"));

		if (null == account) return notFound(res);

		return this.ok(res, summary(account));
	}

	public Object deleteAccount(Request req, Response res) {
		var id = req.params(":accountId");

		if (!storage.has(id)) return notFound(res);
		
		if (!storage.remove(id)) return conflict(res);

		return ok(res);
	}

	public Object getAccountState(Request req, Response res) {
		var account = storage.get(req.params(":accountId"));

		if (null == account) return notFound(res);

		return this.ok(res, locked(account));
	}

	public Object setAccountState(Request req, Response res) {
		AccountState locked = getLocked(req);

		if (null == locked) return badRequest(res);

		var id = req.params(":accountId");

		if (!storage.has(id)) return notFound(res);

		var account = storage.get(id);

		if (locked.isLocked())
			account.lock();
		else
			account.unlock();

		return this.ok(res, locked(account));
	}

	public Object transfer(Request req, Response res) {
		var id = req.params(":accountId");

		if (!storage.has(id)) return notFound(res);

		IncomingTransferRequest body = incomingRequest(req);
		
		if(null == body) return badRequest(res);

		return this.ok(res, this.transfer
				.from(storage.get(id))
				.withRequest(new TransferRequest(UUID.randomUUID().toString(), new BigDecimal(body.getAmount())))
				.execute(storage.get(body.getDestinationAccount())));
	}

	private IncomingTransferRequest incomingRequest(Request req) {
		try {
			return gson.fromJson(req.body(), IncomingTransferRequest.class);
		} catch (Exception e) {
			return null;
		}
	}
	
	private NewAccountRequest newAccountRequest(Request req) {
		try {
			return gson.fromJson(req.body(), NewAccountRequest.class);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private AccountState getLocked(Request req) {
		try {
			return gson.fromJson(req.body(), AccountState.class);
		} catch (Exception e) {
			return null;
		}
	}

	private AccountState locked(Account account) {
		return new AccountState(account.isLocked());
	}

	private Object ok(Response res) {
		return ok(res, "");
	}

	private Object ok(Response res, Object data) {
		res.status(200);
		jsonType(res);
		return data;
	}
	
	private String created(Response res, String path) {
		res.status(201);
		res.header("Location", path);
		return "";
	}

	private String badRequest(Response res) {
		res.status(400);
		return "";
	}

	private String conflict(Response res) {
		res.status(409);
		return "";
	}

	private String notFound(Response res) {
		res.status(404);
		return "";
	}

	private static void jsonType(Response res) {
		res.type("application/json");
	}
}
