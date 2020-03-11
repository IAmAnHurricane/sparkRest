package sparkRest.transfers;

import java.math.BigDecimal;

public class TransferRequest {
	private String operationId;
	private BigDecimal amount;
	
	public TransferRequest(String operationId, BigDecimal amount) {
		this.operationId = operationId;
		this.amount = amount;
	}
	public String getOperationId() {
		return operationId;
	}
	public BigDecimal getAmount() {
		return amount;
	}	
}
