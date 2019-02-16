public class PunyChatTesting
{
  public static void main(String[] args)
  {
    try
    {
      String message = "Hello, world!";
      System.out.println(message);
      byte[] key = CryptoUtil.GetDerivedKey("this is my password");
      byte[] encryptedBytes = CryptoUtil.Encrypt(message.getBytes(), key);
      System.out.println(CryptoUtil.ToBase64String(encryptedBytes));
      byte[] decryptedBytes = CryptoUtil.Decrypt(encryptedBytes, key);
      String decryptedMessage = new String(decryptedBytes, 0, decryptedBytes.length);
      System.out.println(decryptedMessage);
    }
    catch (Exception ex)
    {
      System.err.println("Error: " + ex.getMessage());
    }
  }
}
