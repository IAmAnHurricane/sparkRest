package sparkRest.transfers;

public class TransferResult {
	private boolean success;
	private int code;
	private String message;
	
	public TransferResult(boolean success, int code, String message) {
		this.success = success;
		this.code = code;
		this.message = message;
	}
	public boolean isSuccess() {
		return success;
	}
	public int getCode() {
		return code;
	}
	public String getMessage() {
		return message;
	}
	
	public static TransferResult success() { 
		return new TransferResult(
				true,  
				TransferResultCodes.Success, 
				"Trasnfer succeeded"
			);
	}
	
	public static TransferResult notEnoughResources() {
		return new TransferResult(
			false,  
			TransferResultCodes.SourceAccountNotEnoughResouces, 
			"Could not block requested amount on the source account"
		);
	}
	
	public static TransferResult destinationAccountLocked() {
		return new TransferResult(
				false,  
				TransferResultCodes.DestinationAccountLocked, 
				"The destination account is locked and does not accept transfers"
			);
	}
	
	public static TransferResult destinationAccountNotFound() {
		return new TransferResult(
				false, 
				TransferResultCodes.DestinationAccountNotFound, 
				"The destination account was not found");
	}
	public static TransferResult amountMustBeMoreThanZero() {
		return new TransferResult(
				false, 
				TransferResultCodes.AmountMustBeMoreThanZero, 
				"The transfer amount must be bigger than zero");
	}
}
