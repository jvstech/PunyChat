public class ChatMessage
{
  private String name_ = null;
  private String message_ = null;

  public ChatMessage()
  {
  }

  public ChatMessage(String name, String message)
  {
    name_ = name;
    message_ = message;
  }

  public String name()
  {
    return name_;
  }

  public String message()
  {
    return message_;
  }

  public byte[] toByteArray()
  {
    SerializedBuffer buffer = new SerializedBuffer();
    SerializeTo(buffer);
    return buffer.toByteArray();
  }

  public ChatMessage SerializeTo(SerializedBuffer buffer)
  {
    buffer
      .Write(name_)
      .Write(message_)
      ;
    return this;
  }

  public static ChatMessage FromByteArray(byte[] data)
  {
    if (data == null || data.length == 0)
    {
      return new ChatMessage();
    }

    SerializedBuffer reader = new SerializedBuffer(data);
    String name = reader.ReadString();
    String message = reader.ReadString();
    return new ChatMessage(name, message);
  }
}
