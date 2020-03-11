package sparkRest.transfers;

import sparkRest.accounting.Account;

public interface TransferOperation{
	public TransferResult execute(Account destination);
}