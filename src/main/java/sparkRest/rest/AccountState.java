package sparkRest.rest;

public class AccountState {
	private boolean locked;
	
	public AccountState() {
		this(false);
	}
	
	public AccountState(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
