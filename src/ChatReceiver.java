import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatReceiver extends Thread
{
  // The default buffer size is 4kB, which is the size of one memory page (on
  // most architectures)
  private static final int DEFAULT_BUFFER_SIZE = 4096;
  private static final int MIN_BUFFER_SIZE = 16;

  private DatagramSocket socket_;
  private int bufferSize_;
  private byte[] buffer_;
  private ChatEntryReceived callback_;
  private boolean running_ = false;
  private AtomicBoolean stopRequested_ = new AtomicBoolean(false);


  public ChatReceiver(ChatEntryReceived callback, DatagramSocket chatSocket,
    int bufferSize)
  {
    callback_ = callback;
    socket_ = chatSocket;
    if (bufferSize < MIN_BUFFER_SIZE)
    {
      bufferSize_ = DEFAULT_BUFFER_SIZE;
    }
    else
    {
      bufferSize_ = bufferSize;
    }

    buffer_ = new byte[bufferSize];
  }

  public ChatReceiver(ChatEntryReceived callback, DatagramSocket chatSocket)
  {
    this(callback, chatSocket, DEFAULT_BUFFER_SIZE);
  }

  public boolean isRunning()
  {
    return running_;
  }

  public void setCallback(ChatEntryReceived callback)
  {
    callback_ = callback;
  }

  public void requestStop()
  {
    stopRequested_.set(true);
  }

  public void run()
  {
    try
    {
      if (socket_.getSoTimeout() == 0)
      {
        // timeout of one second
        socket_.setSoTimeout(1000);
      }
    }
    catch (SocketException sockEx)
    {
      sockEx.printStackTrace();
      return;
    }

    running_ = true;
    while (running_)
    {
      DatagramPacket packet = new DatagramPacket(buffer_, buffer_.length);
      try
      {
        socket_.receive(packet);
        byte[] packetData = packet.getData();
        ChatEntry chatEntry = ChatEntry.decrypt(packetData,
          packet.getLength());
        if (chatEntry == null)
        {
          continue;
        }

        callback_.received(chatEntry);
      }
      catch (SocketTimeoutException soToEx)
      {
        if (stopRequested_.get())
        {
          stopRequested_.set(false);
          running_ = false;
          break;
        }
      }
      catch (IOException ioEx)
      {
        // TODO
        ioEx.printStackTrace();
        running_ = false;
        break;
      }
    }
  }
}
