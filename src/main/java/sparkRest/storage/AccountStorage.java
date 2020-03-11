package sparkRest.storage;

import sparkRest.accounting.Account;
import sparkRest.rest.NewAccountRequest;

public interface AccountStorage {

	String newAccount(NewAccountRequest request);

	Account get(String key);

	boolean remove(String id);

	boolean has(String id);

}