import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

public class PunyChat implements Runnable
{
  private Configuration config_;
  private ChatClient client_ = new ChatClient();
  private ChatUIFrame chatUIFrame_ = null;

  public static void main(String[] args)
  {
    EventQueue.invokeLater(new PunyChat());
  }

  @Override
  public void run()
  {
    enableVisualStyles();

    // Show the splash frame
    showSplashFrame();

    // Get the initial configuration
    boolean keepGoing = showConfigurationDialog();
    if (!keepGoing)
    {
      return;
    }

    Channel firstChannel = showAddChannelDialog();
    keepGoing = (firstChannel != null);

    if (keepGoing)
    {
      showMainUI();
    }
  }

  public void showMainUI()
  {
    chatUIFrame_ = new ChatUIFrame(client_);
    chatUIFrame_.setLocationRelativeTo(null);
    chatUIFrame_.pack();
    WindowUtil.center(chatUIFrame_);
    chatUIFrame_.addWindowListener(new WindowListener()
    {
      @Override
      public void windowOpened(WindowEvent e)
      {
      }

      @Override
      public void windowClosing(WindowEvent e)
      {
        if (SwingUtilities.isEventDispatchThread())
        {
          terminate();
        }
        else
        {
          EventQueue.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              terminate();
            }
          });
        }
      }

      @Override
      public void windowClosed(WindowEvent e)
      {
      }

      @Override
      public void windowIconified(WindowEvent e)
      {
      }

      @Override
      public void windowDeiconified(WindowEvent e)
      {
      }

      @Override
      public void windowActivated(WindowEvent e)
      {
      }

      @Override
      public void windowDeactivated(WindowEvent e)
      {
      }
    });

    chatUIFrame_.setPerformConfigurationCallback(new Runnable()
    {
      @Override
      public void run()
      {
        showConfigurationDialog();
      }
    });

    chatUIFrame_.setPerformAddChannelCallback(new Runnable()
    {
      @Override
      public void run()
      {
        Channel channel = showAddChannelDialog();
        if (channel != null)
        {
          chatUIFrame_.getChannelList().addElement(channel);
        }
      }
    });

    chatUIFrame_.setVisible(true);
  }

  public Channel showAddChannelDialog()
  {
    Channel result = null;
    AddChannelDialog dlg = new AddChannelDialog();
    dlg.setLocationRelativeTo(null);
    dlg.pack();
    dlg.setModal(true);
    WindowUtil.center(dlg);
    dlg.setVisible(true);
    if (!dlg.wasCancelled())
    {
      result = dlg.getChannel();
    }

    return result;
  }

  public void showSplashFrame()
  {
    SplashFrame splashFrame = new SplashFrame();
    splashFrame.getContentPane().setBackground(new Color(60, 63, 65));
    splashFrame.setModal(true);
    splashFrame.setLocationRelativeTo(null);
    splashFrame.setVisible(true);
    splashFrame.dispose();
  }

  public boolean showConfigurationDialog()
  {
    boolean result = false;
    ConfigurationDialog dlg = new ConfigurationDialog();
    dlg.setLocationRelativeTo(null);
    dlg.pack();
    dlg.setModal(true);
    WindowUtil.center(dlg);
    dlg.setVisible(true);
    if (!dlg.wasCancelled())
    {
      config_ = dlg.getConfiguration();
      try
      {
        client_.configure(config_);
        result = true;
      }
      catch (IOException ioex)
      {
        showError("Couldn't configure sockets", ioex);
        result = false;
      }
      catch (InterruptedException threadEx)
      {
        showError("Couldn't stop chat receiver", threadEx);
      }
    }

    dlg.dispose();
    return result;
  }

  public void terminate()
  {
    if (client_ != null)
    {
      client_.terminate();
    }

    System.exit(0);
  }

  public static void enableVisualStyles()
  {
    // Try to change the "look and feel" to Nimbus as it looks much nicer
    // than the default "metal" theme. If Nimbus isn't available, just use the
    // default theme.
    try
    {
      // Iterate over all the installed "look and feel" themes
      for (UIManager.LookAndFeelInfo nfo : UIManager.getInstalledLookAndFeels())
      {
        if (("Nimbus").equals(nfo.getName()))
        {
          // We found the Nimbus theme; set it and stop iterating.
          UIManager.setLookAndFeel(nfo.getClassName());
          break;
        }
      }
    }
    catch (ClassNotFoundException | InstantiationException |
      IllegalAccessException | UnsupportedLookAndFeelException ex)
    {
      ex.printStackTrace();
    }
  }

  private static void showError(String message, String title)
  {
    JOptionPane.showMessageDialog(null, message, title,
      JOptionPane.ERROR_MESSAGE);
  }

  private static void showError(String message)
  {
    showError(message, "Error");
  }

  private static void showError(String message, Exception ex)
  {
    if (StringUtil.isNullOrEmpty(message))
    {
      if (ex == null)
      {
        return;
      }

      showError(ex.getMessage());
    }
    else
    {
      if (ex == null)
      {
        showError(message);
      }
      else
      {
        showError(String.format("%s: %s", message, ex.getMessage()));
      }
    }
  }

  private static void showError(Exception ex)
  {
    if (ex != null)
    {
      showError(ex.getMessage());
    }
  }

  private static void runTests()
  {
    //Tests.TestListInterfaces();
    //Tests.TestFirstValidInterface();
    //Tests.TestEncryption();
    //Tests.TestMemoryHash();
    //Tests.TestDecryption();
    //Tests.TestDatagrams();
    //Tests.TestChat();
    //Tests.TestConfigDialog();
    //Tests.TestConfigInput();
    Tests.TestAddChannelDialog();
    Tests.TestChatUI();
  }
}
