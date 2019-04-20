import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

// This is a simple container for data that has been encrypted. It contains the
// encrypted data as well as the initialization vector and the cryptographic
// parameters used to encrypt the data.
public class Ciphertext
{
  private byte[] data_ = null;
  private byte[] iv_ = null;
  private String algorithm_ = null;
  private String transformation_ = null;

  public Ciphertext(byte[] data, byte[] iv, String algorithm,
    String transformation)
  {
    if (data == null || iv == null || iv.length == 0 ||
      StringUtil.isNullOrEmpty(algorithm) ||
      StringUtil.isNullOrEmpty(transformation))
    {
      return;
    }

    data_ = data;
    iv_ = iv;
    algorithm_ = algorithm;
    transformation_ = transformation;
  }

  public Ciphertext(byte[] data, byte[] iv)
  {
    this(data, iv, CryptoUtil.getDefaultCipherAlgorithm(),
      CryptoUtil.getDefaultCipherTransformation());
  }

  public byte[] getData()
  {
    return data_;
  }

  public byte[] getIV()
  {
    return iv_;
  }

  public String getAlgorithm()
  {
    return algorithm_;
  }

  public String getTransformation()
  {
    return transformation_;
  }

  public boolean isValid()
  {
    // data_ can be empty, but it can't be null
    return (data_ != null && iv_ != null && iv_.length > 0 &&
      !StringUtil.isNullOrEmpty(algorithm_) &&
      !StringUtil.isNullOrEmpty(transformation_));
  }

  public byte[] decrypt(byte[] key)
    throws NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeyException, InvalidAlgorithmParameterException,
    IllegalBlockSizeException, BadPaddingException
  {
    return CryptoUtil.decrypt(this, key);
  }

  public byte[] tryDecrypt(byte[] key)
  {
    try
    {
      return CryptoUtil.decrypt(this, key);
    }
    catch (Exception ex)
    {
      System.err.println("Failed to decrypt!");
      ex.printStackTrace();
      return null;
    }
  }

  @Override
  public String toString()
  {
    return String.format(
      "{algorithm = %s; transformation = %s; iv = %s; data = %s}",
      algorithm_, transformation_, CryptoUtil.toHexString(iv_),
      CryptoUtil.toBase64String(data_));
  }
}
