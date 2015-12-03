package org.stellar.base;

import org.apache.commons.codec.binary.Base64;
import org.stellar.base.xdr.AccountID;
import org.stellar.base.xdr.XdrDataOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract class for operations.
 */
public abstract class Operation {
  Operation() {}

  private Keypair mSourceAccount;

  private static final BigDecimal ONE = new BigDecimal(10).pow(7);

  protected static long toXdrAmount(String value) {
    value = checkNotNull(value, "value cannot be null");
    BigDecimal amount = new BigDecimal(value).multiply(Operation.ONE);
    return amount.longValueExact();
  }

  protected static String fromXdrAmount(long value) {
    BigDecimal amount = new BigDecimal(value).divide(Operation.ONE);
    return amount.toPlainString();
  }

  /**
   * Generates Operation XDR object.
   */
  public org.stellar.base.xdr.Operation toXdr() {
    org.stellar.base.xdr.Operation xdr = new org.stellar.base.xdr.Operation();
    if (getSourceAccount() != null) {
      AccountID sourceAccount = new AccountID();
      sourceAccount.setAccountID(getSourceAccount().getXdrPublicKey());
      xdr.setSourceAccount(sourceAccount);
    }
    xdr.setBody(toOperationBody());
    return xdr;
  }

  /**
   * Returns base64-encoded Operation XDR object.
   */
  public String toXdrBase64() {
    try {
      org.stellar.base.xdr.Operation operation = this.toXdr();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      XdrDataOutputStream xdrOutputStream = new XdrDataOutputStream(outputStream);
      org.stellar.base.xdr.Operation.encode(xdrOutputStream, operation);
      Base64 base64Codec = new Base64();
      return base64Codec.encodeAsString(outputStream.toByteArray());
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Returns new Operation object from Operation XDR object.
   * @param xdr XDR object
   */
  public static Operation fromXdr(org.stellar.base.xdr.Operation xdr) {
    org.stellar.base.xdr.Operation.OperationBody body = xdr.getBody();
    Operation operation;
    switch (body.getDiscriminant()) {
      case CREATE_ACCOUNT:
        operation = new CreateAccountOperation.Builder(body.getCreateAccountOp()).build();
        break;
      case PAYMENT:
        operation = new PaymentOperation.Builder(body.getPaymentOp()).build();
        break;
      case PATH_PAYMENT:
        operation = new PathPaymentOperation.Builder(body.getPathPaymentOp()).build();
        break;
      case MANAGE_OFFER:
        operation = new ManagerOfferOperation.Builder(body.getManageOfferOp()).build();
        break;
      case CREATE_PASSIVE_OFFER:
        operation = new CreatePassiveOfferOperation.Builder(body.getCreatePassiveOfferOp()).build();
        break;
      case SET_OPTIONS:
        operation = new SetOptionsOperation.Builder(body.getSetOptionsOp()).build();
        break;
      case CHANGE_TRUST:
        operation = new ChangeTrustOperation.Builder(body.getChangeTrustOp()).build();
        break;
      case ALLOW_TRUST:
        operation = new AllowTrustOperation.Builder(body.getAllowTrustOp()).build();
        break;
      case ACCOUNT_MERGE:
        operation = new AccountMergeOperation.Builder(body).build();
        break;
      default:
        throw new RuntimeException("Unknown operation body " + body.getDiscriminant());
    }
    if (xdr.getSourceAccount() != null) {
      operation.setSourceAccount(Keypair.fromXdrPublicKey(xdr.getSourceAccount().getAccountID()));
    }
    return operation;
  }

  /**
   * Returns operation source account.
   */
  public Keypair getSourceAccount() {
    return mSourceAccount;
  }

  /**
   * Sets operation source account.
   * @param keypair
   */
  void setSourceAccount(Keypair keypair) {
    mSourceAccount = checkNotNull(keypair, "keypair cannot be null");
  }

  /**
   * Generates OperationBody XDR object
   * @return OperationBody XDR object
   */
  abstract org.stellar.base.xdr.Operation.OperationBody toOperationBody();
}
