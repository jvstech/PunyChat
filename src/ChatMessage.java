import java.io.IOException;

public class ChatMessage
{
  private int id_ = 0;
  private int count_ = 0;
  private int fragment_ = 0;
  private String name_ = null;
  private String text_ = null;

  public ChatMessage()
  {
  }

  public ChatMessage(String name, String text)
  {
    name_ = name;
    text_ = text;
  }

  public String getName()
  {
    return name_;
  }

  public String getText()
  {
    return text_;
  }

  public byte[] toByteArray() throws IOException
  {
    SerializedBuffer buffer = new SerializedBuffer();
    serializeTo(buffer);
    return buffer.toByteArray();
  }

  public ChatMessage serializeTo(SerializedBuffer buffer) throws IOException
  {
    buffer
      .write(id_)
      .write(count_)
      .write(fragment_)
      .write(name_)
      .write(text_)
      ;
    return this;
  }

  @Override
  public String toString()
  {
    return String.format("<%s> %s",
      StringUtil.isNullOrEmpty(name_) ? "[]" : name_,
      StringUtil.isNullOrEmpty(text_) ? "" : text_);
  }

  public static ChatMessage fromByteArray(byte[] data)
  {
    if (data == null || data.length == 0)
    {
      return new ChatMessage();
    }

    SerializedBuffer reader = new SerializedBuffer(data);
    try
    {

      int id = reader.readInt();
      int count = reader.readInt();
      int fragment = reader.readInt();
      String name = reader.readString();
      String message = reader.readString();
      ChatMessage result = new ChatMessage(name, message);
      result.id_ = id;
      result.count_ = count;
      result.fragment_ = fragment;
      return result;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
  }
}
