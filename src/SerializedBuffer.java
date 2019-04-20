//!
//! @file     SerializedBuffer.java
//! @author   Jonathan Smith (CIS106-HYB2)
//!

import java.io.*;

public class SerializedBuffer
{
  private ByteIOStream stream_;
  private byte[] inputBuffer_ = null;

  public SerializedBuffer()
  {
    stream_ = new ByteIOStream();
  }

  public SerializedBuffer(byte[] inputData)
  {
    inputBuffer_ = inputData.clone();
    stream_ = new ByteIOStream(inputBuffer_);
  }

  public SerializedBuffer write(byte[] data)
    throws IOException
  {
    if (data != null && data.length > 0)
    {
      writeLEB128Int(data.length);
      stream_.write(data);
    }

    return this;
  }

  // writes a string's bytes to the buffer prefixed with the number of bytes
  // that make up the string
  public SerializedBuffer write(String s)
    throws IOException
  {
    if (s != null && s.length() > 0)
    {
      byte[] stringBytes = s.getBytes();
      write(stringBytes);
    }

    return this;
  }

  public SerializedBuffer write(int value)
    throws IOException
  {
    writeLEB128Int(value);
    return this;
  }

  public byte[] readBytes()
  {
    byte[] result;
    int byteCount;
    int bytesRead = 0;

    try
    {
      byteCount = readLEB128Int();
    }
    catch (NumberFormatException ex)
    {
      // Bad encoding; return null to indicate failure
      return null;
    }

    result = new byte[byteCount];
    while (bytesRead < byteCount)
    {
      bytesRead += stream_.read(result, bytesRead, byteCount - bytesRead);
    }

    return result;
  }

  public String readString()
  {
    String result;
    byte[] stringBytes = readBytes();
    if (stringBytes == null)
    {
      return "";
    }

    result = new String(stringBytes, 0, stringBytes.length);
    return result;
  }

  public int readInt() throws NumberFormatException
  {
    return readLEB128Int();
  }

  public byte[] toByteArray()
  {
    return stream_.toByteArray();
  }

  private void writeLEB128Int(int value) throws IOException
  {
    // A brief explanation of LEB-128 encoding:
    //
    // Values are stored as individual bytes. The actual data of each byte is
    // packed into the rightmost 7 bits; the leftmost bit of each byte is used
    // to indicate if there is another byte (1) or not (0) after the current
    // byte.
    //
    // Because of this encoding, the largest value that can be encoded into a
    // single byte is 127 -- a value of 128 or larger would be using up the
    // leftmost bit for indicating if there are more bytes.
    //
    // For example, the decimal value 5432 can be stored in as few as two bytes
    // without any encoding:
    //
    //    5432 = [00010101b 00111000b] (assume big-endian)
    //
    // To encode 5432 into LEB-128:
    //
    //  1. Since 5432 is >= 128, we know we'll need to use a second byte, so
    //     writeByte the rightmost 7 bits and set the leftmost bit to 1:
    //
    //        5432 AND 0111111b (0x7f) =  56 (00111000b) (0x38)
    //          56 AND 1000000b (0x80) = 184 (10111000b) (0xb8)
    //
    //     So the first byte written is 0xb8.
    //
    //  2. Bitshift the current value to the right 7 bits:
    //
    //        [00010101b 00111000b] >> 7 = [00000000b 00101010b] = 42 (0x2a)
    //
    //  3. Since the current value has dropped below 128, we can just write the
    //     least significant byte as-is and be done:
    //
    //        0xb8 0x2a = [10111000b 00101010b]
    //

    while (Integer.compareUnsigned(value,128) >= 0)
    {
      // use bit-masking when writing the value to ensure only the rightmost 7
      // bits are written while manually setting the leftmost bit.
      stream_.writeByte((byte)((value & 0x7f) | 0x80));
      value >>>= 7;
    }

    stream_.writeByte((byte)(value & 0xff));
  }

  private int readLEB128Int() throws NumberFormatException
  {
    byte currentByte;
    int shiftCount = 0;
    int result = 0;
    do
    {
      // LEB-128 encoded ints can be stored in *at most* 4 bytes. If our shift
      // count reaches 35 (indicating 5 bytes of data), that means the value
      // being decoded is either too large or not actually LEB-128 encoded.
      if (shiftCount == 35)
      {
        throw new NumberFormatException();
      }

      currentByte = stream_.readByte();
      result |= (currentByte & 0x7f) << shiftCount;
      shiftCount += 7;
    } while ((currentByte & 0x80) != 0);

    return result;
  }
}
