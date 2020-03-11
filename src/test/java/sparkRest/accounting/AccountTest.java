package sparkRest.accounting;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

import sparkRest.accounting.InMemoryAccount;

public class AccountTest {
	static final double initialBalance = 123.45d;
	
	private Account account() {
		return account(initialBalance);
	};
	
	private Account account(double balance) {
		return new InMemoryAccount("Account_123", balance);
	}

	@Test
	public void ShouldNotBlockIfAmountIsLessThan0() {
		assertThat(account().block(of(-1), "a"), is(false));
	}

	@Test
	public void ShouldNotBlockIfAmountIs0() {
		assertThat(account().block(of(0), "a"), is(false));
	}

	@Test
	public void ShouldNotBlockIfAmountIsMoreThanCurrentBalance() {
		assertThat(account().block(of(initialBalance + 1), "a"), is(false));
	}

	@Test
	public void ShouldBlockIfAmountIsSameAsCurrentBalance() {
		assertThat(account().block(of(initialBalance),"a"), is(true));
	}

	@Test
	public void ShouldBlockIfAmountIsLessAsCurrentBalance() {
		assertThat(account().block(of(initialBalance - 1),"a"), is(true));
	}

	@Test
	public void ShouldAllowBlockUntilCurrentBalanceBiggerThan0() {
		Account account = account();
		assertThat(account.block(of(initialBalance - 1),"a"), is(true));
		assertThat(account.block(of(1),"b"), is(true));
		assertThat(account.block(of(1),"c"), is(false));
		assertThat(account.getBalance(), equalTo(of(0)));
	}

	@Test 
	public void ShouldReturnCorrectBalanceAfterAmountBlocked() {
		Account account = account();
		account.block(of(initialBalance - 1),"a");
		assertThat(account.getBalance(), equalTo(of(1)));
	}
	
	@Test 
	public void ShouldNotChangeBalanceWhenUnblockForUnknownOperation() {
		var account = account();
		account.unblock("a");
		assertThat(account.getBalance(), equalTo(of(initialBalance)));		
	}
	

	@Test 
	public void ShouldReturnCorrectBalanceAfterAmountUnblocked() {
		Account account = account();
		account.block(of(initialBalance - 3),"a");
		account.block(of(2),"b");
		assertThat(account.getBalance(), equalTo(of(1)));
		account.unblock("b");
		assertThat(account.getBalance(), equalTo(of(3)));
	}
	
	@Test 
	public void ShouldAllowToCommitBlockedAmount() {
		Account account = account();
		account.block(of(initialBalance-1), "abc");
		assertThat(account.getBalance(), equalTo(of(1)));
		account.commitBlockedOperation("abc");
		assertThat(account.getBalance(), equalTo(of(1)));
		account.unblock("abc");		
		assertThat(account.getBalance(), equalTo(of(1)));
	}
	
	@Test
	public void ShouldAllowToAddToAccount() {
		Account account = account(0);
		assertThat(account.getBalance(), equalTo(of(0)));
		account.add(of(initialBalance));
		assertThat(account.getBalance(), equalTo(of(initialBalance)));
	}
	
	@Test
	public void ShouldNotAddWhenAccountLocked() {
		var account = account();
		account.lock();
		assertThat(account.add(of(100)), equalTo(false));
		account.unlock();
		assertThat(account.add(of(100)), equalTo(true));
		assertThat(account.getBalance(), equalTo(of(initialBalance + 100)));
	}
	
	@Test
	public void ShouldSumAndReturnBlockedAmount() {
		var account = account();
		account.block(of(1), "a");
		account.block(of(2), "b");
		account.block(of(3), "c");
		assertThat(account.getBlockedAmount(), is(equalTo(of(6))));
	}
	
	private BigDecimal of(double val) {
		return new BigDecimal(val).setScale(2, RoundingMode.HALF_EVEN);
	}
}
