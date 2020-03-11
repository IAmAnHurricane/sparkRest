package sparkRest.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sparkRest.rest.AccountSummary.summary;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import spark.Request;
import spark.Response;
import sparkRest.accounting.Account;
import sparkRest.accounting.InMemoryAccount;
import sparkRest.storage.AccountStorage;
import sparkRest.transfers.RequestTransfer;
import sparkRest.transfers.Transfer;
import sparkRest.transfers.TransferOperation;
import sparkRest.transfers.TransferRequest;
import sparkRest.transfers.TransferResult;

public class AccountsControllerTest {

	AccountStorage storage;
	Transfer transfer;
	Request sparkRequest;
	Response sparkResponse;
	AccountsController controller;
	String reqPath = "/req/path";
	String newId = "new_id";
	String requestedKey = "1234";

	@Before
	public void before() {
		storage = mock(AccountStorage.class);
		sparkRequest = mock(Request.class);
		sparkResponse = mock(Response.class);
		transfer = mock(Transfer.class);
		controller = new AccountsController(storage, transfer);
		when(sparkRequest.uri()).thenReturn(reqPath);
		when(storage.newAccount(any())).thenReturn(newId);
	}

	@Test
	public void testCreateAccount() {
		var requestedAmount = 145.32d;
		when(sparkRequest.body()).thenReturn("{\"amount\":"+ requestedAmount +"}");

		var obj = controller.createAccount(sparkRequest, sparkResponse);

		assertThat(obj, is(""));
		verify(sparkResponse).status(201);
		verify(sparkResponse).header("Location", reqPath + "/" + newId);
		verify(storage).newAccount(argThat(a -> a.getAmount() == requestedAmount));
	}

	@Test
	public void testCreateAccountReturn400WhenNotParsedBody() {
		when(sparkRequest.body()).thenReturn("{\"amount\":\"notaNumber\"}");

		var obj = controller.createAccount(sparkRequest, sparkResponse);

		assertThat(obj, is(""));
		verify(sparkResponse).status(400);
		verify(storage, never()).newAccount(any());
	}

	@Test
	public void testCreateAccountWithZeroWhenNoBody() {
		when(sparkRequest.body()).thenReturn("{}");

		var obj = controller.createAccount(sparkRequest, sparkResponse);

		assertThat(obj, is(""));
		verify(sparkResponse).status(201);
		verify(storage).newAccount(argThat(a -> a.getAmount() == 0.0d));
	}

	@Test
	public void testCreateAccountReturns400WhenBodyIsEmpty() {
		when(sparkRequest.body()).thenReturn("");

		var obj = controller.createAccount(sparkRequest, sparkResponse);

		assertThat(obj, is(""));
		verify(sparkResponse).status(400);
		verify(storage, never()).newAccount(any());
	}

	@Test
	public void testGetAccountById() {
		var expectedResult = new InMemoryAccount(requestedKey, 123.0);
		
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.get(requestedKey)).thenReturn(expectedResult);
		var obj = controller.getAccountById(sparkRequest, sparkResponse);
		assertThat(obj, is(equalTo(summary(expectedResult))));
		verify(sparkResponse).status(200);
		verify(sparkResponse).type("application/json");
		
		verify(storage).get(requestedKey);		
	}

	@Test
	public void testGetAccountByIdReturns404WhenNotFound() {
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.get(requestedKey)).thenReturn(null);

		var obj = controller.getAccountById(sparkRequest, sparkResponse);
		
		assertThat(obj, is(""));
		verify(sparkResponse).status(404);		
		verify(storage).get(requestedKey);		
	}
	
	@Test
	public void testDeleteAccount() {
		var requestedKey = "1234";
		
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.has(requestedKey)).thenReturn(true);
		when(storage.remove(requestedKey)).thenReturn(true);

		var obj = controller.deleteAccount(sparkRequest, sparkResponse);
		assertThat(obj, is(""));
		verify(sparkResponse).status(200);		
		verify(storage).remove(requestedKey);		
	}	
	
	@Test
	public void testDeleteAccountReturn404WhenAccountNotFound() {
		var requestedKey = "1234";
		
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.has(requestedKey)).thenReturn(false);

		var obj = controller.deleteAccount(sparkRequest, sparkResponse);
		assertThat(obj, is(""));
		verify(sparkResponse).status(404);		
		verify(storage, never()).remove(requestedKey);		
	}
	
	@Test
	public void testDeleteAccountReturn409WhenAccountNotFound() {
		var requestedKey = "1234";
		
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.has(requestedKey)).thenReturn(true);
		when(storage.remove(requestedKey)).thenReturn(false);

		var obj = controller.deleteAccount(sparkRequest, sparkResponse);
		assertThat(obj, is(""));
		verify(sparkResponse).status(409);		
		verify(storage).remove(requestedKey);		
	}

	@Test
	public void testGetAccountStateReturns404WhenNotFound() {
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.get(requestedKey)).thenReturn(null);

		var obj = controller.getAccountState(sparkRequest, sparkResponse);

		assertThat(obj, is(""));
		verify(sparkResponse).status(404);		
	}
	
	@Test
	public void testGetAccountStateNotLocked() {
		var expectedResult = new InMemoryAccount(requestedKey, 123.0);
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.get(requestedKey)).thenReturn(expectedResult);

		var obj = controller.getAccountState(sparkRequest, sparkResponse);
		
		assertThat(obj, is(instanceOf(AccountState.class)));
		assertThat(((AccountState)obj).isLocked(), is(false));
		verify(sparkResponse).status(200);
		verify(sparkResponse).type("application/json");
		verify(storage).get(requestedKey);		
	}

	@Test
	public void testGetAccountStateLocked() {
		var expectedResult = new InMemoryAccount(requestedKey, 123.0);
		expectedResult.lock();
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.get(requestedKey)).thenReturn(expectedResult);
		
		var obj = controller.getAccountState(sparkRequest, sparkResponse);
		
		verify(sparkResponse).status(200);
		assertThat(obj, is(instanceOf(AccountState.class)));
		assertThat(((AccountState)obj).isLocked(), is(true));
		verify(sparkResponse).type("application/json");
		verify(storage).get(requestedKey);		
	}
	
	@Test
	public void testUnlockAccount() {
		var expectedResult = new InMemoryAccount(requestedKey, 0.0);
		expectedResult.lock();
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(sparkRequest.body()).thenReturn(new Gson().toJson(new AccountState()));
		when(storage.has(requestedKey)).thenReturn(true);
		when(storage.get(requestedKey)).thenReturn(expectedResult);
		
		var obj = controller.setAccountState(sparkRequest, sparkResponse);
		
		verify(sparkResponse).status(200);
		assertThat(obj, is(instanceOf(AccountState.class)));
		assertThat(((AccountState)obj).isLocked(), is(false));
		verify(sparkResponse).type("application/json");
		verify(storage).get(requestedKey);		
		verify(storage).has(requestedKey);		
		assertThat(expectedResult.isLocked(), is(false));
	}	

	@Test
	public void testLockAccount() {
		var expectedResult = new InMemoryAccount(requestedKey, 0.0);
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(sparkRequest.body()).thenReturn(new Gson().toJson(new AccountState(true)));
		when(storage.has(requestedKey)).thenReturn(true);
		when(storage.get(requestedKey)).thenReturn(expectedResult);
		
		var obj = controller.setAccountState(sparkRequest, sparkResponse);
		
		verify(sparkResponse).status(200);
		assertThat(obj, is(instanceOf(AccountState.class)));
		assertThat(((AccountState)obj).isLocked(), is(true));
		verify(sparkResponse).type("application/json");
		verify(storage).get(requestedKey);		
		verify(storage).has(requestedKey);		
		assertThat(expectedResult.isLocked(), is(true));
	}	

	@Test
	public void testLockAccountReturns404IfNotFound() {
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(sparkRequest.body()).thenReturn(new Gson().toJson(new AccountState(true)));
		when(storage.has(requestedKey)).thenReturn(false);
		
		controller.setAccountState(sparkRequest, sparkResponse);
		
		verify(sparkResponse).status(404);
		verify(storage, never()).get(requestedKey);		
		verify(storage).has(requestedKey);		
	}	
	
	@Test
	public void testLockAccountReturns400WhenemptyBody() {
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(sparkRequest.body()).thenReturn(new Gson().toJson(null));
		
		controller.setAccountState(sparkRequest, sparkResponse);
		
		verify(sparkResponse).status(400);
		verify(storage, never()).get(requestedKey);		
		verify(storage, never()).has(requestedKey);		
	}	

	@Test
	public void testLockAccountReturns400WhenBadBody() {
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(sparkRequest.body()).thenReturn("{\"locked\":\"alamakota}");
		
		controller.setAccountState(sparkRequest, sparkResponse);
		
		verify(sparkResponse).status(400);
		verify(storage, never()).get(requestedKey);		
		verify(storage, never()).has(requestedKey);		
	}
	
	@Test
	public void testTransferReturns404WhenSourceAccountnotFound() {
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.has(requestedKey)).thenReturn(false);

		controller.transfer(sparkRequest, sparkResponse);
		
		verify(sparkResponse).status(404);
	}	

	@Test
	public void testTransferReturns400WhenRequestIsNotValid() {
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.has(requestedKey)).thenReturn(true);
		when(sparkRequest.body()).thenReturn("{badBody}");

		controller.transfer(sparkRequest, sparkResponse);
		
		verify(sparkResponse).status(400);
		verify(transfer, never()).from(null);
	}
	
	@Test
	public void testSuccesfullTransfer() {
		var destinationId = "123456-abc";
		var source = mock(Account.class);
		var destination = mock(Account.class);
		var requestTransfer = mock(RequestTransfer.class);
		var transferOperation = mock(TransferOperation.class);
		when(sparkRequest.params(":accountId")).thenReturn(requestedKey);
		when(storage.has(requestedKey)).thenReturn(true);
		when(storage.get(requestedKey)).thenReturn(source);
		when(storage.get(destinationId)).thenReturn(destination);
		when(sparkRequest.body()).thenReturn(new Gson().toJson(new IncomingTransferRequest(destinationId, 150.0d)));
		when(transfer.from(any(Account.class))).thenReturn(requestTransfer);
		when(requestTransfer.withRequest(any(TransferRequest.class))).thenReturn(transferOperation);
		when(transferOperation.execute(any(Account.class))).thenReturn(null);
		
		TransferResult obj = (TransferResult) controller.transfer(sparkRequest, sparkResponse);
		
		assertThat(obj, is(nullValue()));
		verify(sparkResponse).status(200);
		verify(sparkResponse).type("application/json");
		verify(transfer).from(source);
		verify(requestTransfer).withRequest(argThat(r -> r.getAmount().equals(new BigDecimal(150)) && !r.getOperationId().isEmpty()));
		verify(transferOperation).execute(destination);		
	}

}
