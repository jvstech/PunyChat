//!
//! @title        Puny Chat chat client
//! @file         ChatClient.java
//! @author       Jonathan Smith (CIS106-HYB2)
//! @description  Acts as a handler for inbound and outbound Puny Chat
//!               socket communications.
//!

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.*;

public class ChatClient
{
  private ChatReceiver receiver_ = null;
  private ChatEntryReceived entryReceivedCallback_ = null;
  private MulticastSocket receiverSocket_ = null;
  private DatagramSocket senderSocket_ = null;
  private InetAddress address_ = null;
  private int port_ = 0;

  public ChatEntryReceived getEntryReceivedCallback()
  {
    return entryReceivedCallback_;
  }

  public void setEntryReceivedCallback(ChatEntryReceived callback)
  {
    entryReceivedCallback_ = callback;
    if (receiver_ != null)
    {
      receiver_.setCallback(entryReceivedCallback_);
    }
  }

  public void configure(Configuration config)
    throws IOException, InterruptedException
  {
    if (config == null)
    {
      return;
    }

    address_ = config.getAddress();
    port_ = config.getPort();

    // Configure the sender socket
    if (senderSocket_ != null)
    {
      senderSocket_.close();
      senderSocket_ = null;
    }

    senderSocket_ = new DatagramSocket();
    if (config.isBroadcast())
    {
      senderSocket_.setBroadcast(true);
    }

    // Turn off the receiver
    if (receiver_ != null)
    {
      if (receiver_.isRunning())
      {
        receiver_.requestStop();
        receiver_.join();
      }

      receiver_ = null;
    }

    // Configure the receiver socket
    if (receiverSocket_ != null && ! receiverSocket_.isClosed())
    {
      receiverSocket_.close();
    }

    receiverSocket_ = new MulticastSocket(port_);
    if (config.isBoundToInterface())
    {
      receiverSocket_.setNetworkInterface(config.getNetworkInterface());
    }

    receiverSocket_.joinGroup(address_);

    // Configure the receiver
    receiver_ = new ChatReceiver(entryReceivedCallback_, receiverSocket_);
    receiver_.start();
  }

  public void send(ChatEntry chatEntry)
    throws IOException, NoSuchPaddingException, NoSuchAlgorithmException,
    IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
    InvalidParameterSpecException
  {
    if (chatEntry == null)
    {
      return;
    }

    if (senderSocket_ == null)
    {
      senderSocket_ = new DatagramSocket();
    }

    byte[] buf = chatEntry.encrypt();
    DatagramPacket packet = new DatagramPacket(buf, buf.length, address_,
      port_);
    senderSocket_.send(packet);
  }

  public void terminate()
  {
    if (senderSocket_ != null && !senderSocket_.isClosed())
    {
      senderSocket_.close();
    }

    senderSocket_ = null;

    if (receiver_ != null && receiver_.isRunning())
    {
      receiver_.requestStop();
      try
      {
        receiver_.join();
      }
      catch (InterruptedException ex)
      {
        // do nothing; we're terminating anyway
      }
    }

    receiverSocket_.close();
    receiverSocket_ = null;
  }

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
