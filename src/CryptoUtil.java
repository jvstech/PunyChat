//!
//! @title        PunyChat
//! @file         CryptoUtil.java
//! @author       Jonathan Smith (CIS106-HYB2)
//! @description  Provides cryptographic ciphering and hashing functionality, as
//!               well as non-cryptographic hash code generation and data
//!               encoding/decoding functionality
//!

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class CryptoUtil
{
  private static final String CIPHER_ALGORITHM = "AES";
  private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5PADDING";
  private static final String HASH_ALGORITHM = "SHA-224";
  private static final String KEY_TYPE = "PBKDF2WithHmacSHA256";
  // FNV1a 64-bit offset basis (FNV-0 value of the string "chongo <")
  private static final long FNV_OFFSET_BASIS_64 = 0xcbf29ce484222325L;
  // FNV1a 64-bit prime (2^40 + 2^8 + 0xb3)
  private static final long FNV_PRIME_64 = 0x100000001b3L;
  // FNV1a 32-bit offset basis (FNV-0 value of the string "chon")
  private static final int FNV_OFFSET_BASIS_32 = 0x811c9dc5;
  // FNV1a 32-bit prime (2^24 + 2^8 + 0x93)
  private static final int FNV_PRIME_32 = 0x1000193;

  public static Ciphertext encrypt(byte[] data, byte[] key)
    throws NoSuchPaddingException, NoSuchAlgorithmException,
    InvalidKeyException, InvalidParameterSpecException, BadPaddingException,
    IllegalBlockSizeException
  {
    if (data == null || data.length == 0 || key == null || key.length == 0)
    {
      return null;
    }

    SecretKey secretKey = new SecretKeySpec(key, CryptoUtil.CIPHER_ALGORITHM);
    Cipher cipher = Cipher.getInstance(CryptoUtil.CIPHER_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] iv =
      cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
    byte[] encryptedData = cipher.doFinal(data);
    Ciphertext result = new Ciphertext(encryptedData, iv,
      secretKey.getAlgorithm(), CryptoUtil.CIPHER_TRANSFORMATION);
    return result;
  }

  public static byte[] decrypt(Ciphertext ciphertext, byte[] key)
    throws NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeyException, InvalidAlgorithmParameterException,
    IllegalBlockSizeException, BadPaddingException
  {
    if (ciphertext == null || !ciphertext.isValid() || key == null ||
      key.length == 0)
    {
      return null;
    }

    IvParameterSpec iv = new IvParameterSpec(ciphertext.getIV());
    SecretKeySpec keySpec = new SecretKeySpec(key, CryptoUtil.CIPHER_ALGORITHM);
    Cipher cipher = Cipher.getInstance(CryptoUtil.CIPHER_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
    byte[] decryptedData = cipher.doFinal(ciphertext.getData());
    return decryptedData;
  }

  public static String getDefaultCipherAlgorithm()
  {
    return CryptoUtil.CIPHER_ALGORITHM;
  }

  public static String getDefaultCipherTransformation()
  {
    return CryptoUtil.CIPHER_TRANSFORMATION;
  }

  public static byte[] getDerivedKey(String key)
  {
    try
    {
      byte[] salt = getFNV1aHash64(key.getBytes());
      SecretKeyFactory keyFactory =
        SecretKeyFactory.getInstance(CryptoUtil.KEY_TYPE);
      KeySpec keySpec =
        new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
      return keyFactory.generateSecret(keySpec).getEncoded();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
  }

  public static byte[] getFNV1aHash64(byte[] data)
  {
    long hashValue = FNV_OFFSET_BASIS_64;
    for (byte b : data)
    {
      hashValue ^= b;
      hashValue *= FNV_PRIME_64;
    }

    return getBytes(hashValue);
  }

  public static byte[] getFNV1aHash32(byte[] data)
  {
    int hashValue = FNV_OFFSET_BASIS_32;
    for (byte b : data)
    {
      hashValue ^= b;
      hashValue *= FNV_PRIME_32;
    }

    return getBytes(hashValue);
  }

  public static byte[] getHash(byte[] data, int rounds)
    throws NoSuchAlgorithmException
  {
    if (data == null || data.length == 0 || rounds <= 0)
    {
      return null;
    }

    MessageDigest sha = MessageDigest.getInstance(CryptoUtil.HASH_ALGORITHM);
    byte[] hash = data.clone();
    for (int i = 0; i < rounds; i++)
    {
      hash = sha.digest(hash);
    }

    return hash;
  }

  public static byte[] getHash(byte[] data)
    throws NoSuchAlgorithmException
  {
    return CryptoUtil.getHash(data, 1);
  }

  public static byte[] getMemoryBoundHash(byte[] data, int fillRounds,
    int jumpRounds)
    throws NoSuchAlgorithmException, IOException
  {
    if (fillRounds < 1)
    {
      fillRounds = 1;
    }

    if (jumpRounds < 1)
    {
      jumpRounds = 1;
    }

    ByteIOStream buffer = new ByteIOStream();
    byte[] hashValue = getHash(data);
    for (int i = 0; i < fillRounds; i++)
    {
      buffer.write(hashValue);
      hashValue = getHash(hashValue);
    }

    int bufferLength = buffer.getLength();
    if (bufferLength <= 8)
    {
      return getHash(buffer.toByteArray());
    }

    int condensedLength = 8;
    buffer.setPosition(-4);
    int offset;
    byte[] offsetData = null;
    byte[] offsetHash = null;
    for (int i = 0; i < jumpRounds; i++)
    {
      offset = getInt(buffer.read(4)) % bufferLength;
      offset -= (offset % condensedLength);
      buffer.setPosition(offset);
      offsetHash = getFNV1aHash64(buffer.read(condensedLength));
      offset = getInt(offsetHash) % bufferLength;
      offset -= (offset % 4);
      buffer.setPosition(offset);
    }

    return getHash(offsetHash);
  }

  public static byte[] getMemoryBoundHash(byte[] data, int rounds)
    throws NoSuchAlgorithmException, IOException
  {
    return getMemoryBoundHash(data, rounds, rounds);
  }

  public static byte[] getMemoryBoundHash(byte[] data)
    throws NoSuchAlgorithmException, IOException
  {
    return getMemoryBoundHash(data,32767, 32767);
  }

  public static String toBase64String(byte[] data)
  {
    return DatatypeConverter.printBase64Binary(data);
  }

  public static String toHexString(byte[] data)
  {
    return DatatypeConverter.printHexBinary(data);
  }

  private static byte[] getBytes(long hashValue)
  {
    byte[] result = new byte[8];
    for (int i = 7; i >= 0; i--)
    {
      result[i] = (byte)(hashValue & 0xff);
      hashValue >>= 8;
    }

    return result;
  }

  private static byte[] getBytes(int hashValue)
  {
    byte[] result = new byte[4];
    for (int i = 3; i >= 0; i--)
    {
      result[i] = (byte)(hashValue & 0xff);
      hashValue >>= 8;
    }

    return result;
  }

  private static int getInt(byte[] intBytes, int offset)
  {
    // Zero-extend the byte array if it isn't at least 4 bytes
    byte[] bytes = new byte[4];
    if (intBytes.length < 4)
    {
      System.arraycopy(intBytes, 0, bytes, 0, intBytes.length);
      Arrays.fill(bytes, intBytes.length, 3, (byte)0);
    }
    else
    {
      System.arraycopy(intBytes, 0, bytes, 0,
        (intBytes.length > 4 ? 4 : intBytes.length));
    }

    return bytes[0] << 24
      | (bytes[1] & 0xff) << 16
      | (bytes[2] & 0xff) << 8
      | (bytes[3] & 0xff);
  }

  private static int getInt(byte[] bytes)
  {
    return getInt(bytes, 0);
  }
}
