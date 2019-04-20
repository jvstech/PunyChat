import com.sun.org.apache.bcel.internal.classfile.Unknown;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Configuration
{
  public static final String DEFAULT_USERNAME = System.getProperty("user.name");
  public static final InetAddress DEFAULT_ADDRESS;
  public static final InetAddress BROADCAST_ADDRESS;
  public static final int DEFAULT_PORT = 64247;

  private String userName_ = DEFAULT_USERNAME;
  private InetAddress address_ = DEFAULT_ADDRESS;
  private boolean broadcast_ = false;
  private int port_ = DEFAULT_PORT;

  static
  {
    InetAddress tmp = null;
    try
    {
      tmp = InetAddress.getByName("225.255.8.0");
    }
    catch (UnknownHostException ex)
    {
      tmp = InetAddress.getLoopbackAddress();
    }

    DEFAULT_ADDRESS = tmp;

    try
    {
      tmp = InetAddress.getByName("255.255.255.255");
    }
    catch (UnknownHostException ex)
    {
      tmp = InetAddress.getLoopbackAddress();
    }

    BROADCAST_ADDRESS = tmp;
  }

  public Configuration()
  {
  }

  public String getUserName()
  {
    return userName_;
  }

  public void setUserName(String userName)
  {
    userName = userName.trim();
    if (userName.length() > 18)
    {
      userName = userName.substring(0, 17);
    }

    userName_ = userName;
  }

  public InetAddress getAddress()
  {
    return address_;
  }

  public void setAddress(InetAddress address)
  {
    if (address == null)
    {
      address = BROADCAST_ADDRESS;
    }

    if (Arrays.equals(address.getAddress(), BROADCAST_ADDRESS.getAddress()))
    {
      broadcast_ = true;
    }
    else
    {
      broadcast_ = false;
    }

    address_ = address;
  }

  public boolean isBroadcast()
  {
    return broadcast_;
  }

  public void setBroadcast(boolean broadcast)
  {
    broadcast_ = broadcast;
    if (broadcast_)
    {
      address_ = BROADCAST_ADDRESS;
    }
    else
    {
      if (Arrays.equals(address_.getAddress(), BROADCAST_ADDRESS.getAddress()))
      {
        address_ = DEFAULT_ADDRESS;
      }
    }
  }

  public int getPort()
  {
    return port_;
  }

  public void setPort(int port)
  {
    if (port < 1)
    {
      port = 1;
    }
    else if (port > 65535)
    {
      port = 65535;
    }

    port_ = port;
  }
}
