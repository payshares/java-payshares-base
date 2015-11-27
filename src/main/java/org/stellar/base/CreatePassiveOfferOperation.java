package org.stellar.base;

import org.stellar.base.xdr.CreatePassiveOfferOp;
import org.stellar.base.xdr.Int64;
import org.stellar.base.xdr.OperationType;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html#create-passive-offer" target="_blank">CreatePassiveOffer</a> operation.
 * @see <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html" target="_blank">List of Operations</a>
 */
public class CreatePassiveOfferOperation extends Operation {
  private final Asset selling;
  private final Asset buying;
  private final Long amount;
  private final String price;

  private CreatePassiveOfferOperation(Asset selling, Asset buying, Long amount, String price) {
    this.selling = checkNotNull(selling, "selling cannot be null");
    this.buying = checkNotNull(buying, "buying cannot be null");
    this.amount = checkNotNull(amount, "amount cannot be null");
    this.price = checkNotNull(price, "price cannot be null");
  }

  /**
   * The asset being sold in this operation
   */
  public Asset getSelling() {
    return selling;
  }

  /**
   * The asset being bought in this operation
   */
  public Asset getBuying() {
    return buying;
  }

  /**
   * Amount of selling being sold.
   */
  public long getAmount() {
    return amount;
  }

  /**
   * Price of 1 unit of selling in terms of buying.
   */
  public String getPrice() {
    return price;
  }

  @Override
  org.stellar.base.xdr.Operation.OperationBody toOperationBody() {
    CreatePassiveOfferOp op = new CreatePassiveOfferOp();
    op.setSelling(selling.toXdr());
    op.setBuying(buying.toXdr());
    Int64 amount = new Int64();
    amount.setInt64(Long.valueOf(this.amount));
    op.setAmount(amount);
    Price price = Price.fromString(this.price);
    op.setPrice(price.toXdr());

    org.stellar.base.xdr.Operation.OperationBody body = new org.stellar.base.xdr.Operation.OperationBody();
    body.setDiscriminant(OperationType.CREATE_PASSIVE_OFFER);
    body.setCreatePassiveOfferOp(op);

    return body;
  }

  /**
   * Builds CreatePassiveOffer operation.
   * @see CreatePassiveOfferOperation
   */
  public static class Builder {

    private final Asset selling;
    private final Asset buying;
    private final long amount;
    private final String price;

    private Keypair mSourceAccount;

    /**
     * Construct a new CreatePassiveOffer builder from a CreatePassiveOfferOp XDR.
     * @param op
     */
    Builder(CreatePassiveOfferOp op) {
      selling = Asset.fromXdr(op.getSelling());
      buying = Asset.fromXdr(op.getBuying());
      amount = op.getAmount().getInt64().longValue();
      int n = op.getPrice().getN().getInt32().intValue();
      int d = op.getPrice().getD().getInt32().intValue();
      price = new BigDecimal(n).divide(new BigDecimal(d)).toString();
    }

    /**
     * Creates a new CreatePassiveOffer builder.
     * @param selling The asset being sold in this operation
     * @param buying The asset being bought in this operation
     * @param amount Amount of selling being sold.
     * @param price Price of 1 unit of selling in terms of buying.
     */
    public Builder(Asset selling, Asset buying, long amount, String price) {
      this.selling = selling;
      this.buying = buying;
      this.amount = amount;
      this.price = price;
    }

    /**
     * Sets the source account for this operation.
     * @param sourceAccount The operation's source account.
     * @return Builder object so you can chain methods.
     */
    public Builder setSourceAccount(Keypair sourceAccount) {
      mSourceAccount = sourceAccount;
      return this;
    }

    /**
     * Builds an operation
     */
    public CreatePassiveOfferOperation build() {
      CreatePassiveOfferOperation operation = new CreatePassiveOfferOperation(selling, buying, amount, price);
      if (mSourceAccount != null) {
        operation.setSourceAccount(mSourceAccount);
      }
      return operation;
    }
  }
}