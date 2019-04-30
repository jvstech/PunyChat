import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Channel
{
  // Global collection of all channels
  private static HashMap<ByteArrayKey, Channel> Channels = new HashMap<>();

  private String name_ = null;
  private byte[] hash_ = null;
  private byte[] key_ = null;

  public Channel()
  {
  }

  private Channel(String name, byte[] hash, byte[] key)
  {
    name_ = name;
    hash_ = hash;
    key_ = key;
    Channels.put(new ByteArrayKey(hash), this);
  }

  public String getName()
  {
    return name_;
  }

  public byte[] getHash()
  {
    return hash_;
  }

  public byte[] getKey()
  {
    return key_;
  }

  @Override
  public String toString()
  {
    return name_;
  }

  public static Channel find(byte[] channelHash)
  {
    ByteArrayKey hashKey;
    try
    {
      hashKey = new ByteArrayKey(channelHash);
      if (Channels.containsKey(hashKey))
      {
        return Channels.get(hashKey);
      }
    }
    catch (NullPointerException ex)
    {
      // ignore and continue
    }

    return null;
  }

  public static Channel fromName(String name)
    throws NoSuchAlgorithmException, IOException
  {
    // Pass the name of the channel as the channel password.
    return fromProtectedName(name, name);
  }

  // This method is used in case we ever want to create a channel using a
  // secret shared key instead of deriving the key from the name of the channel
  // itself.
  public static Channel fromProtectedName(String name, String password)
    throws NoSuchAlgorithmException, IOException
  {
    if (StringUtil.isNullOrEmpty(name))
    {
      return new Channel();
    }

    byte[] hash = CryptoUtil.getMemoryBoundHash(name.getBytes());
    byte[] key = CryptoUtil.getDerivedKey(password);
    return new Channel(name, hash, key);
  }

  public static Channel tryFromName(String name)
  {
    try
    {
      return Channel.fromName(name);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static Channel tryFromProtectedName(String name, String password)
  {
    try
    {
      return Channel.fromProtectedName(name, password);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static Channel[] getAllChannels()
  {
    return Channels.values().toArray(new Channel[0]);
  }
}
