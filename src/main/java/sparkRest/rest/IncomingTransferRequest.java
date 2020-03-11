package sparkRest.rest;

public class IncomingTransferRequest {
	private String destinationAccount;
	private double amount;
	
	public IncomingTransferRequest(String destinationAccount, double amount) {
		super();
		this.destinationAccount = destinationAccount;
		this.amount = amount;
	}

	public String getDestinationAccount() {
		return destinationAccount;
	}

	public double getAmount() {
		return amount;
	}

	public void setDestinationAccount(String destinationAccount) {
		this.destinationAccount = destinationAccount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
	
}
