import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AccountTest {

    @Test
    public void testWithdraw() {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.valueOf(100));

        Assert.assertTrue(account.withdraw(BigDecimal.TEN));
        Assert.assertEquals(BigDecimal.valueOf(90), account.getBalance());
        Assert.assertTrue(account.withdraw(BigDecimal.valueOf(10.55)));
        Assert.assertEquals(BigDecimal.valueOf(79.45), account.getBalance());
        Assert.assertFalse(account.withdraw(BigDecimal.valueOf(100)));
        Assert.assertEquals(BigDecimal.valueOf(79.45), account.getBalance());
    }

    @Test
    public void testDeposit() {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.ZERO);

        Assert.assertTrue(account.deposit(BigDecimal.TEN));
        Assert.assertEquals(BigDecimal.TEN, account.getBalance());
        Assert.assertTrue(account.deposit(BigDecimal.valueOf(123.456)));
        Assert.assertEquals(BigDecimal.valueOf(133.456), account.getBalance());
    }

    @Test
    public void testGetUser() {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.ZERO);

        Assert.assertEquals("alex", account.getUser());
    }

    @Test
    public void testGetCurrency() {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.ZERO);

        Assert.assertEquals(Currency.getInstance("USD"), account.getCurrency());
    }

    @Test
    public void testGetBalance() {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.ZERO);

        Assert.assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    @Test
    public void testSetBalance() {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.ZERO);

        Assert.assertEquals(BigDecimal.ZERO, account.getBalance());
        account.setBalance(BigDecimal.TEN);
        Assert.assertEquals(BigDecimal.TEN, account.getBalance());
    }

    @Test(expected = NullPointerException.class)
    public void testNullUser() {
        Account account = new Account(null, Currency.getInstance("USD"), BigDecimal.TEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUser() {
        Account account = new Account("", Currency.getInstance("USD"), BigDecimal.TEN);
    }

    @Test(expected = NullPointerException.class)
    public void testNullCurrency() {
        Account account = new Account("alex", null, BigDecimal.TEN);
    }

    @Test(expected = NullPointerException.class)
    public void testNullBalance() {
        Account account = new Account("alex", Currency.getInstance("USD"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBalance() {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.valueOf(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroAmountWithdraw(){
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);
        account.withdraw(BigDecimal.ZERO);
    }

    @Test
    public void testDepositUpdatedConcurrently() throws InterruptedException {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.ZERO);

        ExecutorService tasker = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++){
            tasker.execute(() -> account.deposit(BigDecimal.TEN));
        }

        tasker.shutdown();

        if (tasker.awaitTermination(60, TimeUnit.SECONDS)){
            Assert.assertEquals(BigDecimal.valueOf(100), account.getBalance());
        }
    }

    @Test
    public void testWithdrawUpdatedConcurrently() throws InterruptedException {
        Account account = new Account("alex", Currency.getInstance("USD"), BigDecimal.valueOf(90));

        ExecutorService tasker = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++){
            tasker.execute(() -> account.withdraw(BigDecimal.TEN));
        }

        tasker.shutdown();

        if (tasker.awaitTermination(60, TimeUnit.SECONDS)){
            Assert.assertEquals(BigDecimal.ZERO, account.getBalance());
        }
    }
}