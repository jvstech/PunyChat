//!
//! @title        Puny Chat chat client
//! @file         ChatClient.java
//! @author       Jonathan Smith (CIS106-HYB2)
//! @description  Acts as a handler for inbound and outbound Puny Chat
//!               socket communications.
//!

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class ChatClient
{
  private HashMap<byte[], String> channelKeys_ = new HashMap<>();

  public static List<NetworkInterface> getValidInterfaces()
  {
    List<NetworkInterface> validInterfaces = new ArrayList<>();
    Enumeration<NetworkInterface> netInterfaces;
    try
    {
      netInterfaces = NetworkInterface.getNetworkInterfaces();
    }
    catch (SocketException ex)
    {
      // Can't get a list of interfaces, so return an empty list.
      return validInterfaces;
    }

    for (NetworkInterface netInt : Collections.list(netInterfaces))
    {
      try
      {
        if (!netInt.isVirtual() && !netInt.isLoopback() &&
          !netInt.isPointToPoint() && netInt.isUp())
        {
          // It's a "real" interface and it's up; good to use
          validInterfaces.add(netInt);
        }
      }
      catch (SocketException ex)
      {
        // Ignore the error; we probably don't want to use this interface.
        ex.printStackTrace();
      }
    }

    return validInterfaces;
  }

  public static List<InetAddress> getValidHostAddresses()
  {
    List<InetAddress> validAddresses = new ArrayList<>();
    List<NetworkInterface> netInts = ChatClient.getValidInterfaces();
    for (NetworkInterface netInt : netInts)
    {
      for (InetAddress netAddr : Collections.list(netInt.getInetAddresses()))
      {
        if (!netAddr.isLoopbackAddress())
        {
          validAddresses.add(netAddr);
        }
      }
    }

    return validAddresses;
  }

  public static List<InetAddress> getValidHostIPv4Addresses()
  {
    List<InetAddress> validIPv4Addresses = new ArrayList<>();
    List<InetAddress> validAddresses = getValidHostAddresses();
    for (InetAddress netAddr : validAddresses)
    {
      if (netAddr.getAddress().length == 4)
      {
        validIPv4Addresses.add(netAddr);
      }
    }

    return validIPv4Addresses;
  }

  public static InetAddress getDefaultAddress()
  {
    List<NetworkInterface> netInts = ChatClient.getValidInterfaces();
    for (NetworkInterface netInt : netInts)
    {
      // Filter out VMware adapters on purpose
      if (!netInt.getDisplayName().startsWith("VMware"))
      {
        for (InetAddress netAddr : Collections.list(netInt.getInetAddresses()))
        {
          // Find an IPv4 address
          if (netAddr.getAddress().length == 4)
          {
            return netAddr;
          }
        }
      }
    }

    // Couldn't find a good IPv4 address; return the first one found at all.
    if (netInts.isEmpty())
    {
      return null;
    }

    return netInts.get(0).getInetAddresses().nextElement();
  }
}
