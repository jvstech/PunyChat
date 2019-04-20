import java.util.Arrays;

public class ByteArrayKey
{
  private final byte[] data;

  public ByteArrayKey(byte[] data)
  {
    if (data == null)
    {
      throw new NullPointerException();
    }

    this.data = data;
  }

  @Override
  public boolean equals(Object rhs)
  {
    if (!(rhs instanceof ByteArrayKey))
    {
      return false;
    }

    return Arrays.equals(data, ((ByteArrayKey)rhs).data);
  }

  @Override
  public int hashCode()
  {
    return Arrays.hashCode(data);
  }
}
