//!
//! @title        Puny Chat Configuration
//! @file         Configuration.java
//! @author       Jonathan Smith (CIS106-HYB2)
//! @description  Provides a way of setting common settings related to the
//!               operation of Puny Chat.
//!

import com.sun.org.apache.bcel.internal.classfile.Unknown;

import javax.xml.bind.annotation.XmlElementDecl;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Configuration
{
  public enum CommunicationType
  {
    MULTICAST,
    BROADCAST,
    DIRECT
  }

  public static final String DEFAULT_USERNAME = System.getProperty("user.name");
  public static final InetAddress DEFAULT_ADDRESS;
  public static final InetAddress GLOBAL_LISTEN_ADDRESS;
  public static final InetAddress BROADCAST_ADDRESS;
  public static final int DEFAULT_PORT = 64247;

  private String userName_ = DEFAULT_USERNAME;
  private InetAddress address_ = DEFAULT_ADDRESS;
  private CommunicationType communicationType_ = CommunicationType.MULTICAST;
  private boolean boundToInterface_ = false;
  private NetworkInterface networkInterface_ = null;
  private int port_ = DEFAULT_PORT;

  // Static constructor
  static
  {
    // Using a temporary variable avoids the issue of a member not having
    // been possibly assigned.
    InetAddress tmp = null;
    try
    {
      // This is an arbitrarily chosen multicast IPv4 address
      tmp = InetAddress.getByName("225.255.8.0");
    }
    catch (UnknownHostException ex)
    {
      ex.printStackTrace();
      tmp = InetAddress.getLoopbackAddress();
    }

    DEFAULT_ADDRESS = tmp;

    try
    {
      // This is the "any" address (INADDR_ANY), which will listen on any
      // interface.
      tmp = InetAddress.getByName("0.0.0.0");
    }
    catch (UnknownHostException ex)
    {
      ex.printStackTrace();
      tmp = InetAddress.getLoopbackAddress();
    }

    GLOBAL_LISTEN_ADDRESS = tmp;

    try
    {
      // This is the IPv4 limited broadcast address
      tmp = InetAddress.getByName("255.255.255.255");
    }
    catch (UnknownHostException ex)
    {
      ex.printStackTrace();
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
    // The user name can be no longer than 18 characters, so trim it down if
    // it's longer.
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

    // If the chosen address is the limited broadcast address, set broadcast_
    // field to true to indicate this.
    if (Arrays.equals(address.getAddress(), BROADCAST_ADDRESS.getAddress()))
    {
      setBroadcast();
    }
    else if (address.isMulticastAddress())
    {
      setMulticast(address);
    }
    else
    {
      setDirect(address);
    }

    address_ = address;
  }

  public boolean isBroadcast()
  {
    return (communicationType_ == CommunicationType.BROADCAST);
  }

  public void setBroadcast()
  {
    communicationType_ = CommunicationType.BROADCAST;
    address_ = BROADCAST_ADDRESS;
  }

  public boolean isDirect()
  {
    return (communicationType_ == CommunicationType.DIRECT);
  }

  public void setDirect(InetAddress address)
  {
    communicationType_ = CommunicationType.DIRECT;
    address_ = address;
  }

  public boolean isBoundToInterface()
  {
    return boundToInterface_;
  }

  public boolean isMulticast()
  {
    return (communicationType_ == CommunicationType.MULTICAST);
  }

  public void setMulticast(InetAddress address)
  {
    communicationType_ = CommunicationType.MULTICAST;
    address_ = address;
  }

  public NetworkInterface getNetworkInterface()
  {
    return networkInterface_;
  }

  public void setNetworkInterface(NetworkInterface networkInterface)
  {
    boundToInterface_ = (networkInterface != null);
    networkInterface_ = networkInterface;
  }

  public int getPort()
  {
    return port_;
  }

  public void setPort(int port)
  {
    // Port numbers range from 1 to 65535. Technically, port 0 could work, but
    // it's not meant to be used for direct communications.
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

  @Override
  public String toString()
  {
    return String.format(
      "{ userName = %s; netInterface = %s; address = %s; port = %d }",
      getUserName(), getNetworkInterface(), getAddress(), getPort());
  }

  public static Configuration fromStandardInput()
  {
    Scanner stdin = new Scanner(System.in);
    Configuration config = new Configuration();
    boolean validInput = false;

    // User name
    System.out.printf("User name [%s]: ", config.getUserName());
    String userName = stdin.nextLine().trim();
    if (!userName.isEmpty())
    {
      config.setUserName(userName);
    }

    // Broadcast mode
    System.out.printf("Use broadcast mode [%s]? ",
      config.isBroadcast() ? "Y/n" : "y/N");
    String useBroadcast = stdin.nextLine().trim();
    if (!useBroadcast.isEmpty() && useBroadcast.toLowerCase().startsWith("y"))
    {
      config.setBroadcast();
    }

    // Direct mode
    System.out.printf("Use direct communcation mode [%s]? ",
      config.isDirect() ? "Y/n" : "y/N");
    String useDirect = stdin.nextLine().trim();
    if (!useDirect.isEmpty() && useDirect.toLowerCase().startsWith("y"))
    {
      while (!validInput)
      {
        System.out.printf("Remote address [%s]: ", config.getAddress());
        String remoteAddrString = stdin.nextLine().trim();
        if (remoteAddrString.isEmpty())
        {
          validInput = true;  // Not necessary, but conveys intent.
          break;
        }

        try
        {
          InetAddress address = InetAddress.getByName(remoteAddrString);
          config.setAddress(address);
          validInput = true;
        }
        catch (UnknownHostException ex)
        {
          // Shouldn't matter, but display a message anyway.
          System.err.println("Unknown address: " + remoteAddrString);
        }
      }

      validInput = false;
    }

    // Multicast address (if not broadcast or direct mode)
    if (!config.isBroadcast() && !config.isDirect())
    {
      while (!validInput)
      {
        System.out.printf("Multicast address [%s]: ", config.getAddress());
        String multicastAddrString = stdin.nextLine().trim();
        if (multicastAddrString.isEmpty())
        {
          validInput = true;  // Not necessary, but conveys intent.
          break;
        }

        try
        {
          InetAddress address = InetAddress.getByName(multicastAddrString);
          if (!address.isMulticastAddress())
          {
            System.err.println("Not a valid multicast address: " +
              multicastAddrString);
            continue;
          }

          config.setAddress(address);
          validInput = true;
        }
        catch (UnknownHostException ex)
        {
          // Shouldn't matter, but display a message anyway
          System.err.println("Unknown address: " + multicastAddrString);
        }
      }

      validInput = false;
    }

    // Bind to specific network interface
    List<NetworkInterface> networkInterfaces = ChatClient.getValidInterfaces();
    if (!networkInterfaces.isEmpty())
    {
      System.out.printf("Bind to a specific network interface [%s]? ",
        config.isBoundToInterface() ? "Y/n" : "y/N");
      String useInterface = stdin.nextLine().trim();
      if (useInterface.isEmpty())
      {
        // Skip the input loop
        validInput = true;
      }

      while (!validInput)
      {
        System.out.println("0: cancel - do not bind to an interface");
        int currentInterfaceIdx = 0;
        for (int i = 1; i <= networkInterfaces.size(); i++)
        {
          NetworkInterface netInt = networkInterfaces.get(i - 1);
          if (config.isBoundToInterface() &&
            netInt.toString().equals(config.getNetworkInterface().toString()))
          {
            currentInterfaceIdx = i;
          }

          System.out.printf("%d: %s\n", i, networkInterfaces.get(i - 1));
        }

        System.out.printf("Choose an interface [%d]: ",
          currentInterfaceIdx);
        try
        {
          String selectedInterfaceString = stdin.nextLine().trim();
          if (selectedInterfaceString.isEmpty())
          {
            // use the default
            validInput = true;  // Not necessary, but conveys intent.
            break;
          }

          int selectedInterface =
            Integer.parseUnsignedInt(selectedInterfaceString);
          if (selectedInterface > networkInterfaces.size())
          {
            System.err.println("Not a valid choice.");
            continue;
          }

          if (selectedInterface == 0)
          {
            config.setNetworkInterface(null);
          }
          else
          {
            config.setNetworkInterface(networkInterfaces.get(
              selectedInterface - 1));
          }

          validInput = true;
        }
        catch (NumberFormatException ex)
        {
          System.err.println("Not a valid choice.");
        }
      }

      validInput = false;
    }

    // Port number
    while (!validInput)
    {
      System.out.printf("Port number [%d]: ", config.getPort());
      String portNumberString = stdin.nextLine();
      if (portNumberString.isEmpty())
      {
        validInput = true;  // Not necessary, but conveys intent.
        break;
      }

      try
      {
        int portNumber = Integer.parseUnsignedInt(portNumberString);
        if (portNumber == 0 || portNumber > 65535)
        {
          System.err.println(
            "Not a valid port number. Ports can range from 1 to 65535.");
          continue;
        }

        config.setPort(portNumber);
        validInput = true;
      }
      catch (NumberFormatException ex)
      {
        System.err.println(
          "Not a valid port number. Ports can range from 1 to 65535.");
      }
    }

    return config;
  }
}
