package sparkRest.transfers;

import sparkRest.accounting.Account;

public interface TransferSource {
	public RequestTransfer to(Account source);
}