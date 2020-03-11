package sparkRest.accounting;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InMemoryAccount implements Account {
	private String id;
	private Balance balance;
	private boolean locked = false;
	
	public InMemoryAccount(String id, double balance) {
		this.id = id;
		this.balance = new Balance(normalize(new BigDecimal(balance)));
	}
	
	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public synchronized void unblock(String operationId) {
		if (!balance.hasBlockedOperation(operationId)) return;
		
		this.balance = this.balance.unblock(operationId);
	}

	@Override
	public synchronized boolean block(BigDecimal amount, String operationId) {
		var newAmount = normalize(amount);
		if(!this.balance.canBlock(newAmount)) return false;
	
		this.balance = this.balance.block(newAmount, operationId);
		return true;
	}
	
	@Override
	public BigDecimal getBalance() {
		return this.balance.current();
	}

	@Override
	public synchronized boolean add(BigDecimal amount) {
		if (this.locked) return false;
		
		this.balance = this.balance.add(normalize(amount));
		return true;
	}

	@Override
	public synchronized void commitBlockedOperation(String operationId) {
		this.balance = this.balance.commitBlockedOperation(operationId);
	}		
	
	@Override
	public void lock() {
		this.locked = true;
	}

	@Override
	public void unlock() {
		this.locked = false;
	}

	private BigDecimal normalize(BigDecimal amount) {
		return amount.setScale(2, RoundingMode.HALF_EVEN);
	}

	@Override
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	public BigDecimal getBlockedAmount() {
		return this.balance.getBlockedAmount();
	}

	@Override
	public boolean isEmpty() {
		return this.balance.isEmpty();
	}

	public Object getSummary() {
		// TODO Auto-generated method stub
		return null;
	}
}