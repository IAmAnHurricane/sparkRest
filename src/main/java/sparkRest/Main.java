package sparkRest;

import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import sparkRest.rest.AccountsController;
import sparkRest.storage.InMemoryAccountStorage;
import sparkRest.transfers.Transfer;

public class Main {
	public static void main(String[] args) {
		port(8008);
		staticFiles.location("/docs");
		path("/api/v1", new AccountsController(new InMemoryAccountStorage(), new Transfer()).setupRoutes());
	}
}