package sparkRest.transfers;

import java.math.BigDecimal;

import sparkRest.accounting.Account;

public class Transfer {
	public RequestTransfer from(Account source) {
		return new RequestTransfer() {			
			@Override
			public TransferOperation withRequest(TransferRequest request) {
				return new TransferOperation() {					
					@Override
					public TransferResult execute(Account destination) {
						if (null == destination)
							return TransferResult.destinationAccountNotFound();

						if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0)
							return TransferResult.amountMustBeMoreThanZero();
						
						if (!source.block(request.getAmount(), request.getOperationId()))
							return TransferResult.notEnoughResources();

						if (!destination.add(request.getAmount())) {
							source.unblock(request.getOperationId());
							return TransferResult.destinationAccountLocked();
						}

						source.commitBlockedOperation(request.getOperationId());
						return TransferResult.success();
					}
				};
			}
		};
	}
}