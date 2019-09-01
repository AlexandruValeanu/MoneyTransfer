import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Transfer {

    /**
     * Unique ID of the transfer.
     */
    public final UUID id;

    /**
     * Source account.
     */
    private final Account source;

    /**
     * Destination account.
     */
    private final Account destination;

    /**
     * Amount of money to be transferred.
     */
    private final BigDecimal amount;

    public Transfer(Account source, Account destination, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.source = Objects.requireNonNull(source, "Source account cannot be null");
        this.destination = Objects.requireNonNull(destination, "Destination account cannot be null");

        if (this.source.equals(this.destination)){
            throw new IllegalArgumentException("Source and destination accounts must differ");
        }

        if (!this.source.getCurrency().equals(this.destination.getCurrency())){
            throw new IllegalArgumentException("Source and destination accounts must have the same currency");
        }

        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    /**
     * Get the source account.
     *
     * @return the source account
     */
    public Account getSource() {
        return source;
    }

    /**
     * Get the destination account.
     *
     * @return the destination account
     */
    public Account getDestination() {
        return destination;
    }

    /**
     * Get the amount to be transferred.
     *
     * @return the amount to be transferred
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Execute the transfer.
     *
     * @return <code>true</code> if the transfer was successful; <code>false</code> otherwise
     */
    boolean execute(){
        Lock sourceLock = source.getLock();
        Lock destLock = destination.getLock();

        try {
            if (sourceLock.tryLock(Constants.LOCK_WAIT_TIME, TimeUnit.MILLISECONDS)){
                try {
                    if (destLock.tryLock(Constants.LOCK_WAIT_TIME, TimeUnit.MILLISECONDS)) {
                        try {
                            if (source.withdraw(amount)){
                                if (destination.deposit(amount)){
                                    return true;
                                }
                            }

                        } finally {
                            destLock.unlock();
                        }
                    }
                } finally {
                    sourceLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "id=" + id +
                ", source=" + source.id +
                ", destination=" + destination.id +
                ", amount=" + amount +
                '}';
    }
}
