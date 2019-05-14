import javax.swing.*;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Tests
{
  public static void TestListInterfaces()
  {
    List<NetworkInterface> netInts = ChatClient.getValidInterfaces();
    for (NetworkInterface netInt : netInts)
    {
      for (InetAddress netAddr : Collections.list(netInt.getInetAddresses()))
      {
        System.out.println(netInt.getDisplayName() + ": " + netAddr.toString());
      }
    }
  }

  public static void TestFirstValidInterface()
  {
    try
    {
      List<NetworkInterface> netInts = ChatClient.getValidInterfaces();
      for (NetworkInterface netInt : netInts)
      {
        if (!netInt.getDisplayName().startsWith("VMware"))
        {
          for (InetAddress netAddr : Collections.list(netInt.getInetAddresses()))
          {
            if (netAddr.getAddress().length == 4)
            {
              System.out.println("Default interface: " + netAddr.toString());
              return;
            }
          }
        }
      }
    }
    catch (Exception ex)
    {
      System.err.println("Error: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public static void TestEncryption()
  {
    try
    {
      String message = "Hello, world!";
      System.out.println("Plain text: " + message);
      byte[] key = CryptoUtil.getDerivedKey("this is my password");
      Ciphertext ciphertext = CryptoUtil.encrypt(message.getBytes(), key);
      System.out.println("Ciphertext: " + ciphertext);
      byte[] decryptedBytes = CryptoUtil.decrypt(ciphertext, key);
      String decryptedMessage = new String(decryptedBytes, 0,
        decryptedBytes.length);
      System.out.println("Decrypted: " + decryptedMessage);
    }
    catch (Exception ex)
    {
      System.err.println("Error: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public static void TestMemoryHash()
  {
    try
    {
      String message = "Hello, world!";
      System.out.println("Plain text: " + message);
      byte[] hashValue = CryptoUtil.getMemoryBoundHash(message.getBytes());
      System.out.println("Memory-bound hash: " +
        CryptoUtil.toHexString(hashValue));
    }
    catch (Exception ex)
    {
      System.err.println("Error: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public static void TestDecryption()
  {
    try
    {
      Channel channel = Channel.fromName("general");
      ChatMessage message = new ChatMessage("jvsmith", "Hello, world!");
      ChatEntry entry = new ChatEntry(channel, message);
      System.out.println(entry);
      byte[] encryptedData = entry.encrypt();
      ChatEntry decryptedEntry = ChatEntry.decrypt(encryptedData,
        encryptedData.length);
      if (decryptedEntry != null)
      {
        System.out.println(decryptedEntry);
      }
      else
      {
        System.err.println("Failed to decrypt chat entry!");
      }
    }
    catch (Exception ex)
    {
      System.err.println("Error: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public static void TestDatagrams()
  {
    Thread receiverThread = new Thread()
    {
      private MulticastSocket serverSocket;
      private InetAddress group;
      private byte[] buffer = new byte[512];
      public void run()
      {
        try
        {
          serverSocket = new MulticastSocket(64247);
          group = InetAddress.getByName("225.225.8.0");
          serverSocket.joinGroup(group);
          while (true)
          {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            String received = new String(packet.getData(), 0,
              packet.getLength());
            if (received.equals("quit"))
            {
              break;
            }

            System.out.println("Received: " + received);
          }

          serverSocket.leaveGroup(group);
          serverSocket.close();
        }
        catch (Exception ex)
        {
          System.err.println("Receiver socket error!");
          ex.printStackTrace();
        }
      }
    };

    receiverThread.start();

    Scanner scanner = new Scanner(System.in);
    try
    {
      InetAddress group = InetAddress.getByName("225.225.8.0");
      DatagramSocket socket = new DatagramSocket();
      while (true)
      {
        System.out.print("Message: ");
        String msg = scanner.nextLine();
        byte[] buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group,
          64247);
        socket.send(packet);
        if (msg.equals("quit"))
        {
          break;
        }
      }

      socket.close();
      receiverThread.join();
    }
    catch (Exception ex)
    {
      System.err.println("Sender socket error!");
      ex.printStackTrace();
    }
  }

  public static void TestChat()
  {
    final String userName = System.getProperty("user.name");
    final String groupAddress = "225.225.8.0";
    final String channelName = "programming";
    final int port = 64247;
    try
    {
      InetAddress group = InetAddress.getByName(groupAddress);
      Channel channel = Channel.fromName(channelName);
      MulticastSocket receiverSocket = new MulticastSocket(port);
      receiverSocket.joinGroup(group);
      ChatEntryReceived callback = new ChatEntryReceived()
      {
        @Override
        public void received(ChatEntry entry)
        {
          System.out.println(entry);
        }
      };

      ChatReceiver receiver = new ChatReceiver(callback, receiverSocket);
      receiver.start();

      DatagramSocket clientSocket = new DatagramSocket();
      Scanner scanner = new Scanner(System.in);
      while (receiver.isRunning())
      {
        String messageText = scanner.nextLine();
        if (messageText.equals("/quit"))
        {
          receiver.requestStop();
          break;
        }

        ChatMessage message = new ChatMessage(userName, messageText);
        ChatEntry chatEntry = new ChatEntry(channel, message);
        byte[] buf = chatEntry.encrypt();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group,
          port);
        clientSocket.send(packet);
      }

      clientSocket.close();
      receiver.join();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public static void TestConfigDialog()
  {
    ConfigurationDialog dlg = new ConfigurationDialog();
    dlg.setLocationRelativeTo(null);
    dlg.pack();
    dlg.setModal(true);
    dlg.setVisible(true);
    if (dlg.wasCancelled())
    {
      System.out.println("Configuration was cancelled.");
    }
    else
    {
      //System.out.println(String.format("User name: %s\nAddress: %s\nPort: %d",
      //  dlg.getConfiguration().getUserName(),
      //  dlg.getConfiguration().getAddress(),
      //  dlg.getConfiguration().getPort()));
      System.out.println(dlg.getConfiguration().toString());
    }

    dlg.dispose();
  }

  public static void TestConfigInput()
  {
    Configuration config = Configuration.fromStandardInput();
    System.out.println("Configuration:");
    System.out.println(config);
  }

  public static void TestAddChannelDialog()
  {
    AddChannelDialog dlg = new AddChannelDialog();
    dlg.pack();
    dlg.setModal(true);
    dlg.setVisible(true);
    if (!dlg.wasCancelled())
    {
      System.out.println("Channel: " + dlg.getChannel());
    }
    else
    {
      System.out.println("Channel dialog was cancelled.");
    }

    dlg.dispose();
  }

  public static void TestChatUI()
  {
    Channel.tryFromName("c++");
    Channel.tryFromName("programming");
    Channel.tryFromName("java");
    Channel.tryFromName("general");
    ChatUIFrame chatUI = new ChatUIFrame(new ChatClient());
    chatUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    chatUI.setLocationRelativeTo(null);
    chatUI.pack();
    chatUI.setVisible(true);
  }
}
