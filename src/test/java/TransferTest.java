import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;

public class TransferTest {

    @Test
    public void testGetSource() {
        Account accountAlex = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);
        Account accountBen = new Account("ben", Currency.getInstance("USD"), BigDecimal.ZERO);

        Transfer transfer = new Transfer(accountAlex, accountBen, BigDecimal.ONE);
        Assert.assertEquals(accountAlex, transfer.getSource());
    }

    @Test
    public void testGetDestination() {
        Account accountAlex = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);
        Account accountBen = new Account("ben", Currency.getInstance("USD"), BigDecimal.ZERO);

        Transfer transfer = new Transfer(accountAlex, accountBen, BigDecimal.ONE);
        Assert.assertEquals(accountBen, transfer.getDestination());
    }

    @Test
    public void testGetAmount() {
        Account accountAlex = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);
        Account accountBen = new Account("ben", Currency.getInstance("USD"), BigDecimal.ZERO);

        Transfer transfer = new Transfer(accountAlex, accountBen, BigDecimal.ONE);
        Assert.assertEquals(BigDecimal.ONE, transfer.getAmount());
    }

    @Test
    public void testExecute() {

    }

    @Test(expected = NullPointerException.class)
    public void testNullSourceAccount() {
        Account accountBen = new Account("ben", Currency.getInstance("USD"), BigDecimal.ZERO);

        new Transfer(null, accountBen, BigDecimal.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testNullDestinationAccount() {
        Account accountAlex = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);

        new Transfer(accountAlex, null, BigDecimal.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testNullAmount() {
        Account accountAlex = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);
        Account accountBen = new Account("ben", Currency.getInstance("USD"), BigDecimal.ZERO);

        new Transfer(accountAlex, accountBen, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroAmount() {
        Account accountAlex = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);
        Account accountBen = new Account("ben", Currency.getInstance("USD"), BigDecimal.ZERO);

        new Transfer(accountAlex, accountBen, BigDecimal.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSameAccount() {
        Account accountAlex = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);

        new Transfer(accountAlex, accountAlex, BigDecimal.ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDifferentCurrency() {
        Account accountAlex = new Account("alex", Currency.getInstance("USD"), BigDecimal.TEN);
        Account accountBen = new Account("ben", Currency.getInstance("CAD"), BigDecimal.ZERO);

        new Transfer(accountAlex, accountBen, BigDecimal.ONE);
    }
}