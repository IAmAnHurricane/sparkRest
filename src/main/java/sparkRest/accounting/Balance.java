package sparkRest.accounting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Balance {
	private BigDecimal available;
	private Map<String, BigDecimal> blockedOperations;

	public Balance(BigDecimal available) {
		this(available, new HashMap<String, BigDecimal>());
	}

	private Balance(BigDecimal available, Map<String, BigDecimal> blockedOperations) {
		this.available = available;
		this.blockedOperations = blockedOperations;
	}
	
	public boolean canBlock(BigDecimal amount) {
		return amount.compareTo(BigDecimal.ZERO) > 0 &&
			this.available.subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
	}

	public Balance block(BigDecimal amount, String operationId) {
		var newBlockedOperations = new HashMap<String, BigDecimal>(this.blockedOperations);
		newBlockedOperations.put(operationId, amount);
		return new Balance(
				this.available.subtract(amount),
				newBlockedOperations
			);
	}

	public BigDecimal current() {
		return this.available;
	}

	public boolean hasBlockedOperation(String operationId) {
		return this.blockedOperations.containsKey(operationId);
	}

	public Balance unblock(String id) {
		var newBlockedOperations = new HashMap<String, BigDecimal>(this.blockedOperations);
		var amount = newBlockedOperations.remove(id);
		return new Balance(
				this.available.add(amount),
				newBlockedOperations);
	}

	public Balance add(BigDecimal amount) {
		var newBlockedOperations = new HashMap<String, BigDecimal>(this.blockedOperations);
		return new Balance(
				this.available.add(amount),
				newBlockedOperations);
	}

	public Balance commitBlockedOperation(String operationId) {
		var newBlockedOperations = new HashMap<String, BigDecimal>(this.blockedOperations);
		newBlockedOperations.remove(operationId);
		return new Balance(
				this.available,
				newBlockedOperations);
	}

	public BigDecimal getBlockedAmount() {
		var result = this.blockedOperations.values().stream().reduce((c1, c2) -> c1.add(c2));
		
		if (result.isPresent()) return result.get();
		
		return BigDecimal.ZERO;
	}

	public boolean isEmpty() {
		return this.available.add(this.getBlockedAmount()).compareTo(BigDecimal.ZERO) == 0;
	}
}
