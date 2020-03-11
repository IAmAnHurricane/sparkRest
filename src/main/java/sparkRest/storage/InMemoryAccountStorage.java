package sparkRest.storage;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import sparkRest.accounting.Account;
import sparkRest.accounting.InMemoryAccount;
import sparkRest.rest.NewAccountRequest;

public class InMemoryAccountStorage implements AccountStorage {
	private static AccountStorage instance = null;
	
	public static AccountStorage create() {
		if (null == instance)
			instance = new InMemoryAccountStorage();
		
		return instance;
	}	
	
	ConcurrentHashMap<String, Account> accounts;	
	
	public InMemoryAccountStorage() {
		this.accounts = new ConcurrentHashMap<String, Account>();
	}

	@Override
	public String newAccount(NewAccountRequest request) {
		var key = UUID.randomUUID().toString();
		this.accounts.put(key, new InMemoryAccount(key, request.getAmount()));
		return key;
	}
	
	@Override
	public Account get(String key) {
		return this.accounts.get(key);
	}

	@Override
	public boolean remove(String id) {
		var account = this.accounts.get(id);
		
		if(!account.isLocked()) return false;
		
		if (!account.isEmpty()) return false;
			
		accounts.remove(id);
		return true;
	}

	@Override
	public boolean has(String id) {
		return this.accounts.containsKey(id);
	}
}
