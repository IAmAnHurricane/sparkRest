package sparkRest.rest;

import java.math.BigDecimal;

import sparkRest.accounting.Account;

public class AccountSummary {
	String id;
	BigDecimal available;
	BigDecimal blocked;
	boolean isLocked;
	
	private AccountSummary(Account account) {
		this.id = account.getId();
		this.available = account.getBalance();
		this.blocked = account.getBlockedAmount();
		this.isLocked = account.isLocked();
	}
	
	public String getId() {
		return id;
	}
	public BigDecimal getAvailable() {
		return available;
	}
	public BigDecimal getBlocked() {
		return blocked;
	}
	public boolean isLocked() {
		return isLocked;
	}

	public static AccountSummary summary(Account account) {
		return new AccountSummary(account);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((available == null) ? 0 : available.hashCode());
		result = prime * result + ((blocked == null) ? 0 : blocked.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isLocked ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountSummary other = (AccountSummary) obj;
		if (available == null) {
			if (other.available != null)
				return false;
		} else if (!available.equals(other.available))
			return false;
		if (blocked == null) {
			if (other.blocked != null)
				return false;
		} else if (!blocked.equals(other.blocked))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isLocked != other.isLocked)
			return false;
		return true;
	}
	
	
}
