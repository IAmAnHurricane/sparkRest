package sparkRest.transfers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import static org.hamcrest.MatcherAssert.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import sparkRest.accounting.Account;

public class TransferTest {

	Account fromMock;
	Account toMock;

	String operationId = "op-123";
	double operationAmount = 123.45d;
	TransferRequest request = new TransferRequest(operationId, of(operationAmount));
	Transfer transfer; 
	
	@Before
	public void before() {
		fromMock = mock(Account.class);
		toMock = mock(Account.class);
		transfer = new Transfer();
	}
	
	@Test
	public void testSuccesfullTransfer() {
		when(fromMock.block(any(), eq(operationId))).thenReturn(true);
		when(toMock.add(any())).thenReturn(true);
		var result = transfer.from(fromMock).withRequest(request).execute(toMock);
		
		verify(fromMock).block(of(operationAmount), operationId);
		verify(toMock).add(of(operationAmount));
		verify(fromMock).commitBlockedOperation(operationId);		
		
		assertResult(TransferResult.success(), result);
	}


	@Test
	public void testTransferNotMadeWhenBlockFails() {
		when(fromMock.block(any(), eq(operationId))).thenReturn(false);
		var result = transfer.from(fromMock).withRequest(request).execute(toMock);
		
		verify(fromMock).block(of(operationAmount), operationId);
		verify(toMock, never()).add(of(operationAmount));
		verify(fromMock, never()).commitBlockedOperation(operationId);		
		
		assertResult(TransferResult.notEnoughResources(), result);
	}	

	@Test
	public void testTransferRolledBackWhenAddFails() {
		when(fromMock.block(any(), eq(operationId))).thenReturn(true);
		when(toMock.add(any())).thenReturn(false);
		var result = transfer.from(fromMock).withRequest(request).execute(toMock);
		
		verify(fromMock).block(of(operationAmount), operationId);
		verify(toMock).add(of(operationAmount));
		verify(fromMock, never()).commitBlockedOperation(operationId);		
		verify(fromMock).unblock(operationId);		
		
		assertResult(TransferResult.destinationAccountLocked(), result);
	}	
	
	@Test 
	public void testTransferFailedWhenDestinationIsNull() {
		when(fromMock.block(any(), eq(operationId))).thenReturn(true);
		var result = transfer.from(fromMock).withRequest(request).execute(null);
		
		verify(fromMock, never()).block(of(operationAmount), operationId);
		verify(fromMock, never()).commitBlockedOperation(operationId);		
		verify(fromMock, never()).unblock(operationId);		
		
		assertResult(TransferResult.destinationAccountNotFound(), result);
	}
	
	@Test 
	public void testTransferFailedWhenAmountIs0() {
		when(fromMock.block(any(), eq(operationId))).thenReturn(true);
		var request = new TransferRequest("abc", of(0));
		var result = transfer.from(fromMock).withRequest(request).execute(toMock);
		
		verify(fromMock, never()).block(of(operationAmount), operationId);
		verify(fromMock, never()).commitBlockedOperation(operationId);		
		verify(fromMock, never()).unblock(operationId);		
		
		assertResult(TransferResult.amountMustBeMoreThanZero(), result);
	}
	
	@Test 
	public void testTransferFailedWhenAmountIsLessThan0() {
		when(fromMock.block(any(), eq(operationId))).thenReturn(true);
		var request = new TransferRequest("abc", of(-1));
		var result = transfer.from(fromMock).withRequest(request).execute(toMock);
		
		verify(fromMock, never()).block(of(operationAmount), operationId);
		verify(fromMock, never()).commitBlockedOperation(operationId);		
		verify(fromMock, never()).unblock(operationId);		
		
		assertResult(TransferResult.amountMustBeMoreThanZero(), result);
	}

	private void assertResult(TransferResult expected, TransferResult actual) {
		assertThat("Result.isSuccess", actual.isSuccess(), is(equalTo(expected.isSuccess())));
		assertThat("Result.geetCode", actual.getCode(), is(equalTo(expected.getCode())));
		assertThat("Result.getMessage", actual.getMessage(), is(equalTo(expected.getMessage())));
	}

	private BigDecimal of(double amount) {
		return new BigDecimal(amount).setScale(2, RoundingMode.HALF_EVEN);
	}
}

