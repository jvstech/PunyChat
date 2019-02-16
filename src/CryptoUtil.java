import java.security.*;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class CryptoUtil
{
  private static final String CipherMode = "AES/CBC/PKCS5PADDING";
  private static final String KeyType = "PBKDF2WithHmacSHA256";

  public static byte[] Encrypt(byte[] data, byte[] key) throws Exception
  {
    if (data == null || data.length == 0 || key == null || key.length == 0)
    {
      return null;
    }

    byte[] result = null;
    SecretKey secretKey = new SecretKeySpec(key,"AES");
    Cipher cipher = Cipher.getInstance(CryptoUtil.CipherMode);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] iv =
      cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
    byte[] encryptedData = cipher.doFinal(data);
    SerializedBuffer outBuffer = new SerializedBuffer();
    outBuffer
      .Write(iv)
      .Write(encryptedData)
      ;
    result = outBuffer.toByteArray();
    return result;
  }

  public static byte[] Decrypt(byte[] data, byte[] key) throws Exception
  {
    if (data == null || data.length == 0 || key == null || key.length == 0)
    {
      return null;
    }

    SerializedBuffer inBuffer = new SerializedBuffer(data);
    byte[] ivData = inBuffer.ReadBytes();
    if (ivData == null)
    {
      return null;
    }

    IvParameterSpec iv = new IvParameterSpec(ivData);
    byte[] encryptedData = inBuffer.ReadBytes();
    if (encryptedData == null)
    {
      return null;
    }

    SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
    Cipher cipher = Cipher.getInstance(CryptoUtil.CipherMode);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
    byte[] decryptedData = cipher.doFinal(encryptedData);
    return decryptedData;
  }

  public static byte[] GetHashRounds(byte[] data, int rounds)
    throws NoSuchAlgorithmException
  {
    if (data == null || data.length == 0 || rounds <= 0)
    {
      return null;
    }

    MessageDigest sha = MessageDigest.getInstance("SHA-224");
    byte[] hash = data.clone();
    for (int i = 0; i < rounds; i++)
    {
      hash = sha.digest(hash);
    }

    return hash;
  }

  public static byte[] GetHashRounds(byte[] data)
    throws NoSuchAlgorithmException
  {
    return CryptoUtil.GetHashRounds(data, 1);
  }

  public static byte[] GetDerivedKey(String key)
  {
    try
    {

      SecretKeyFactory keyFactory =
        SecretKeyFactory.getInstance(CryptoUtil.KeyType);
      KeySpec keySpec =
        new PBEKeySpec(key.toCharArray(), new byte[8], 65536, 256);
      return keyFactory.generateSecret(keySpec).getEncoded();
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static String ToBase64String(byte[] data)
  {
    return DatatypeConverter.printBase64Binary(data);
  }

  public static String ToHexString(byte[] data)
  {
    return DatatypeConverter.printHexBinary(data);
  }
}
