package sparkRest.transfers;

public class TransferResultCodes {

	public static final int Success = 0;
	public static final int SourceAccountNotEnoughResouces = 1;
	public static final int DestinationAccountLocked = 2;
	public static final int DestinationAccountNotFound = 3;
	public static final int AmountMustBeMoreThanZero = 4;	
}