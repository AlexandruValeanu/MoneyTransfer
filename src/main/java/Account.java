import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
    /**
     * Unique ID of the account.
     */
    public final UUID id;

    /**
     * Name of the user who owns the account.
     */
    private final String user;

    /**
     * The currency associated with the account.
     */
    private final Currency currency;

    /**
     * The current balance of the account.
     */
    private BigDecimal balance;

    /**
     * The lock used to perform changes on the balance safely.
     */
    private transient Lock lock;

    public Account(String user, Currency currency, BigDecimal balance) {
        this.id = UUID.randomUUID();
        this.user = Objects.requireNonNull(user, "User cannot be null");

        if (user.isEmpty()){
            throw new IllegalArgumentException("User cannot be empty");
        }

        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        this.balance = Objects.requireNonNull(balance, "Balance cannot be null");

        if (balance.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Balance must be non-negative");
        }

        this.lock = new ReentrantLock();
    }

    /**
     * Withdraw a given amount from the account's balance.
     *
     * @param amount the amount to be withdrawn
     * @return <code>true</code> if the operation was successful; <code>false</code> otherwise
     * @throws NullPointerException if amount is <code>null</code>
     * @throws IllegalArgumentException if amount is not greater than zero
     */
    public boolean withdraw(BigDecimal amount){
        Objects.requireNonNull(amount, "Amount cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        try {
            lock.lock();
            if (balance.compareTo(amount) >= 0){
                balance = balance.subtract(amount);
                return true;
            }
        } finally {
            lock.unlock();
        }

        return false;
    }

    /**
     * Deposit a given amount from the account's balance.
     *
     * @param amount the amount to be deposited
     * @return <code>true</code> if the operation was successful; <code>false</code> otherwise
     * @throws NullPointerException if amount is <code>null</code>
     * @throws IllegalArgumentException if amount is not greater than zero
     */
    public boolean deposit(BigDecimal amount){
        Objects.requireNonNull(amount, "Amount cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        try {
            lock.lock();
            balance = balance.add(amount);
        } finally {
            lock.unlock();
        }

        return true;
    }

    /**
     * Get the user's name.
     *
     * @return the user's name
     */
    public String getUser() {
        return user;
    }

    /**
     * Get the account's currency.
     *
     * @return the account's currency
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Get the account's balance.
     *
     * @return the account's balance
     */
    public BigDecimal getBalance() {
        try {
            lock.lock();
            return balance;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set the account's balance to a new value.
     *
     * @param balance the new balance
     * @throws NullPointerException if balance is <code>null</code>
     * @throws IllegalArgumentException if balance is negative
     */
    public void setBalance(BigDecimal balance) {
        Objects.requireNonNull(balance, "Balance cannot be null");

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance must be non-negative");
        }

        try {
            lock.lock();
            this.balance = balance;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the account's lock.
     *
     * @return the account's lock
     */
    public Lock getLock() {
        return lock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", currency=" + currency +
                ", balance=" + getBalance() +
                '}';
    }
}
