package sparkRest.accounting;

import java.math.BigDecimal;

public interface Account {

	String getId();
	BigDecimal getBalance();
	boolean block(BigDecimal amount, String operationId);
	void unblock(String operationId);
	void commitBlockedOperation(String operationId);
	boolean add(BigDecimal amount);
	void lock();
	void unlock();
	boolean isLocked();
	BigDecimal getBlockedAmount();
	boolean isEmpty();
}