package sparkRest.rest;

public class NewAccountRequest {
	private double amount;

	public NewAccountRequest() {
		this(0.0d);
	}
	
	public NewAccountRequest(double amount) {
		this.amount = amount;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
}
