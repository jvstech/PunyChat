import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

public class ChatEntry
{
  private static final int MIN_PACKET_SIZE = 49;
  private static final int MAX_PACKET_SIZE = 512;

  private Channel channel_ = null;
  private ChatMessage message_ = null;

  public ChatEntry()
  {
  }

  public ChatEntry(Channel channel, ChatMessage message)
  {
    channel_ = channel;
    message_ = message;
  }

  public Channel getChannel()
  {
    return channel_;
  }

  public ChatMessage getMessage()
  {
    return message_;
  }

  public byte[] encrypt()
    throws NoSuchPaddingException, NoSuchAlgorithmException,
    IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
    InvalidParameterSpecException, IOException
  {
    byte[] messageData = message_.toByteArray();
    Ciphertext ciphertext = CryptoUtil.encrypt(messageData, channel_.getKey());
    ByteIOStream outStream = new ByteIOStream();
    // Packet layout:
    //   1 = channel hash
    //   2 = initialization vector
    //   3 = encrypted data
    outStream.write(channel_.getHash());
    outStream.write(ciphertext.getIV());
    outStream.write(ciphertext.getData());
    return outStream.toByteArray();
  }

  @Override
  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    String separator = "";
    if (channel_ != null)
    {
      buffer.append("#" + channel_);
      separator = " ";
    }

    if (message_ != null)
    {
      buffer.append(separator);
      buffer.append(message_);
    }

    return buffer.toString();
  }

  public static ChatEntry decrypt(byte[] packetBuffer, int packetLength)
  {
    if (packetLength < MIN_PACKET_SIZE || packetLength > MAX_PACKET_SIZE)
    {
      // Packet is too short or too long to be a chat message
      return null;
    }

    ByteIOStream packetStream = new ByteIOStream(packetBuffer, packetLength);
    // Read the first 28 bytes to try to determine the channel
    byte[] channelHash = packetStream.read(28);
    Channel channel = Channel.find(channelHash);
    if (channel == null)
    {
      // Not a channel key
      return null;
    }

    byte[] channelKey = channel.getKey();
    // Read the initialization vector
    byte[] channelIv = packetStream.read(16);
    byte[] encryptedData = packetStream.readToEnd();
    Ciphertext ciphertext = new Ciphertext(encryptedData, channelIv);
    byte[] data = ciphertext.tryDecrypt(channelKey);
    if (data == null)
    {
      // Couldn't decrypt the packet
      return null;
    }

    ChatMessage message = ChatMessage.fromByteArray(data);
    return new ChatEntry(channel, message);
  }
}
