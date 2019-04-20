//!
//! @title        PunyChat
//! @file         ByteIOStream.java
//! @author       Jonathan Smith (CIS106-HYB2)
//! @description  Provides a byte stream that can be both written to and read
//!               from without having to create separate streams.
//!

import java.io.IOException;
import java.util.Arrays;

public class ByteIOStream
{
  // maximum buffer capacity (56 bytes shy of 2 GB)
  private static final int MAX_BUFFER_CAPACITY = 0x7fffffc7;
  // backing storage array
  private byte[] buffer_ = new byte[0];
  // read/writeByte index
  private int position_ = 0;
  // length of the stream
  private int length_ = 0;
  // length of the backing buffer
  private int capacity_ = 0;

  public ByteIOStream()
  {
  }

  public ByteIOStream(byte[] buffer)
  {
    buffer_ = buffer.clone();
    capacity_ = buffer_.length;
    length_ = buffer_.length;
  }

  public ByteIOStream(byte[] buffer, int length)
  {
    if (length >= buffer.length)
    {
      buffer_ = buffer.clone();
      capacity_ = buffer_.length;
      length_ = buffer_.length;
      return;
    }

    buffer_ = new byte[length];
    System.arraycopy(buffer, 0, buffer_, 0, length);
    capacity_ = buffer_.length;
    length_ = buffer_.length;
  }

  public int getLength()
  {
    return length_;
  }

  public int getPosition()
  {
    return position_;
  }

  public void setPosition(int position)
    throws IllegalArgumentException
  {
    if (position < 0)
    {
      // Seek from the end instead of the front
      position_ = length_ + position;
    }
    else
    {
      position_ = position;
    }
  }

  public int seekBegin()
  {
    setPosition(0);
    return getPosition();
  }

  public int seekEnd()
  {
    if (length_ > 0)
    {
      setPosition(length_);
    }
    else
    {
      setPosition(0);
    }

    return getPosition();
  }

  public int read(byte[] buffer, int offset, int count)
  {
    if (buffer == null)
    {
      throw new NullPointerException();
    }

    if (offset < 0)
    {
      offset = 0;
    }

    if (count < 0)
    {
      count = 0;
    }

    if ((buffer.length - offset) < count)
    {
      count = buffer.length - offset;
    }

    int byteCount = length_ - position_;
    if (byteCount > count)
    {
      byteCount = count;
    }

    if (byteCount < 0)
    {
      return 0;
    }

    ByteIOStream.copy(buffer_, position_, buffer, offset, byteCount);
    position_ += byteCount;
    return byteCount;
  }

  public byte[] read(int count)
  {
    if (count <= 0)
    {
      return new byte[0];
    }

    byte[] result = new byte[count];
    read(result, 0, count);
    return result;
  }

  public byte readByte()
  {
    byte[] result = new byte[1];
    read(result, 0, 1);
    return result[0];
  }

  public byte[] readToEnd()
  {
    return read(length_ - position_);
  }

  public void write(byte[] buffer, int offset, int count)
    throws IOException
  {
    if (buffer == null)
    {
      throw new NullPointerException();
    }

    if (offset < 0)
    {
      offset = 0;
    }

    if (count < 0)
    {
      count = 0;
    }

    int newLength = position_ + count;
    if (newLength < 0)
    {
      // Overflow
      throw new IOException("Stream too long");
    }

    if (newLength > length_)
    {
      boolean needsZeroing = position_ > length_;
      if (newLength > capacity_)
      {
        boolean resized = ensureCapacity(newLength);
        if (resized)
        {
          needsZeroing  = false;
        }
      }

      if (needsZeroing)
      {
        Arrays.fill(buffer_, length_, newLength, (byte)0);
      }

      length_ = newLength;
    }

    ByteIOStream.copy(buffer, offset, buffer_, position_, count);
    position_ = newLength;
  }

  public void write(byte[] buffer)
    throws IOException
  {
    write(buffer, 0, buffer.length);
  }

  // writeByte a single byte
  public void writeByte(byte b)
    throws IOException
  {
    // Originally, I was going to just put the byte into a single-element array
    // and writeByte that, but that's actually less efficient than doing the
    // calculations needed to just writeByte the byte to the buffer and update the
    // position, length, and capacity as needed.
    if (position_ >= length_)
    {
      int newLength = position_ + 1;
      if (newLength < 0)
      {
        throw new IOException("Stream too long");
      }

      boolean needsZeroing = (position_ > length_);
      if (newLength > capacity_)
      {
        boolean resized = ensureCapacity(newLength);
        if (resized)
        {
          needsZeroing = false;
        }
      }

      if (needsZeroing)
      {
        Arrays.fill(buffer_, length_, newLength, (byte)0);
      }

      length_ = newLength;
    }

    buffer_[position_++] = b;
  }

  public byte[] toByteArray()
  {
    int length = length_;
    // Sanity check
    if (length > buffer_.length)
    {
      length = buffer_.length;
    }

    byte[] result = new byte[length];
    System.arraycopy(buffer_, 0, result, 0, length);
    return result;
  }

  private boolean ensureCapacity(int capacity)
  {
    if (capacity < 0)
    {
      capacity = 0;
    }

    if (capacity <= capacity_)
    {
      return false;
    }

    int newCapacity = capacity;
    if (newCapacity < 256)
    {
      newCapacity = 256;
    }

    // MAX_BUFFER_CAPACITY is set to 56 bytes short of 0x7fffffff to allow for
    // slight overflowing (without actual overflowing).
    if (newCapacity < (capacity_ * 2))
    {
      newCapacity = capacity_ * 2;
    }

    if ((capacity_ * 2) > MAX_BUFFER_CAPACITY)
    {
      newCapacity = (capacity > MAX_BUFFER_CAPACITY
        ? capacity
        : MAX_BUFFER_CAPACITY);
    }

    if (newCapacity > 0)
    {
      byte[] dstBuffer = new byte[newCapacity];
      if (length_ > 0)
      {
        System.arraycopy(buffer_, 0, dstBuffer, 0, length_);
      }

      buffer_ = dstBuffer;
    }
    else
    {
      buffer_ = null;
    }

    capacity_ = newCapacity;
    return true;
  }

  public static void copy(byte[] srcBuffer, int srcIndex, byte[] dstBuffer,
    int dstIndex, int count)
  {
    if (count <= 8)
    {
      // For sizes of 8 or less, just copy one byte at a time.
      int bytesLeft = count;
      while (--bytesLeft >= 0)
      {
        dstBuffer[dstIndex + bytesLeft] = srcBuffer[srcIndex + bytesLeft];
      }
    }
    else
    {
      // This lowers to a native call, so it usually results in an aligned,
      // multibyte copy -- meaning it's typically faster than manually
      // copying the bytes individually.
      System.arraycopy(srcBuffer, srcIndex, dstBuffer, dstIndex, count);
    }
  }
}
