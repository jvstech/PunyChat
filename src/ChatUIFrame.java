import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

// I want to "upgrade" this to use GroupLayout, but I'm not sure I have enough
// time.

public class ChatUIFrame extends JFrame
{
  // components
  private JList<Channel> channelList;
  private DefaultListModel<Channel> channelListModel = new DefaultListModel<>();
  private JTextArea messagesText;
  private JTextField messageText;
  // property fields
  private HashMap<Channel, ArrayList<ChatMessage>> chatMessages_ =
    new HashMap<>();
  private ChatClient client_ = null;
  private Channel lastSelectedChannel_ = null;

  public ChatUIFrame(ChatClient chatClient)
  {
    client_ = chatClient;
    initialize();
    client_.setEntryReceivedCallback(new ChatEntryReceived()
    {
      @Override
      public void received(ChatEntry entry)
      {
        if (SwingUtilities.isEventDispatchThread())
        {
          onChatEntryReceived(entry);
        }
        else
        {
          EventQueue.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              onChatEntryReceived(entry);
            }
          });
        }
      }
    });
  }

  public DefaultListModel<Channel> getChannelList()
  {
    return channelListModel;
  }

  private Channel getSelectedChannel()
  {
    if (channelList.isSelectionEmpty())
    {
      if (lastSelectedChannel_ != null &&
        channelListModel.contains(lastSelectedChannel_))
      {
       channelList.setSelectedValue(lastSelectedChannel_, true);
      }
      else if (!channelListModel.isEmpty())
      {
        channelList.setSelectedIndex(0);
        lastSelectedChannel_ = channelList.getSelectedValue();
      }
      else
      {
        // Can't do anything. No channels to select.
        return null;
      }
    }

    return channelList.getSelectedValue();
  }

  private Channel getLastSelectedChannel()
  {
    return lastSelectedChannel_;
  }

  private void onChatEntryReceived(ChatEntry entry)
  {
    ChatMessage message = entry.getMessage();
    Channel channel = entry.getChannel();
    chatMessages_.getOrDefault(channel, new ArrayList<>()).add(message);
    if (getSelectedChannel() == channel)
    {
      messagesText.append(String.format("%s\n", message));
      messagesText.setCaretPosition(messagesText.getDocument().getLength());
    }
  }

  private void initialize()
  {
    try
    {
      setTitle("Puny Chat");
      setLayout(new GridBagLayout());
      setLocationRelativeTo(null);

      GridBagConstraints rootConstraints = new GridBagConstraints();

      // Button panel (buttons along the top of the frame)
      rootConstraints.gridx = 0;
      rootConstraints.gridy = 0;
      JPanel buttonPanel;
      add((buttonPanel = new JPanel()), rootConstraints);
      buttonPanel.setLayout(new FlowLayout());

      // Content panel (everything below the button panel)
      rootConstraints.gridy++;
      rootConstraints.fill = GridBagConstraints.BOTH;
      rootConstraints.weightx = 1;
      rootConstraints.weighty = 1;
      JPanel contentPanel;
      add((contentPanel = new JPanel()), rootConstraints);

      // Channel list
      GridBagConstraints contentConstraints = new GridBagConstraints();
      contentConstraints.gridx = 0;
      contentConstraints.gridy = 0;
      contentConstraints.weighty = 1;
      contentConstraints.anchor = GridBagConstraints.NORTH;
      contentConstraints.fill = GridBagConstraints.VERTICAL;
      contentPanel.add((channelList = new JList<>(channelListModel)),
        contentConstraints);
      for (Channel channel : Channel.getAllChannels())
      {
        channelListModel.addElement(channel);
      }
      channelList.addListSelectionListener(new ListSelectionListener()
      {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
          int idx = e.getFirstIndex();
          Channel channel = channelListModel.get(idx);
          lastSelectedChannel_ = channel;
          StringBuilder sb = new StringBuilder();
          if (chatMessages_.containsKey(channel))
          {
            for (ChatMessage message : chatMessages_.get(channel))
            {
              sb.append(message.toString() + "\n");
            }
          }

          messagesText.setText(sb.toString());
          messagesText.setCaretPosition(messagesText.getDocument().getLength());
        }
      });

      // Chat panel (contains messages for the current channel and the input
      // text box)
      contentConstraints.gridx++;
      contentConstraints.weighty = 1;
      contentConstraints.weightx = 1;
      contentConstraints.fill = GridBagConstraints.HORIZONTAL;
      JPanel chatPanel;
      contentPanel.add((chatPanel = new JPanel()), contentConstraints);
      chatPanel.setLayout(new GridBagLayout());

      // Messages text area
      GridBagConstraints chatConstraints = new GridBagConstraints();
      chatConstraints.gridx = 0;
      chatConstraints.gridy = 0;
      chatConstraints.weightx = 1;
      chatConstraints.weighty = 1;
      chatConstraints.fill = GridBagConstraints.BOTH;
      chatPanel.add((messagesText = new JTextArea(40, 80)), chatConstraints);
      messagesText.setLineWrap(true);
      messagesText.setWrapStyleWord(true);
      messagesText.setEditable(false);

      // Chat input textbox
      chatConstraints.gridy++;
      chatConstraints.weightx = 1;
      chatConstraints.weighty = 0;
      chatConstraints.fill = GridBagConstraints.HORIZONTAL;
      chatPanel.add((messageText = new JTextField(80)), chatConstraints);
      messageText.addActionListener(new ActionListener()
      {
        // Enter keypress handler
        @Override
        public void actionPerformed(ActionEvent e)
        {
          Channel channel = getSelectedChannel();
          ChatMessage message = new ChatMessage(client_.getUserName(),
            messageText.getText());
          ChatEntry chatEntry = new ChatEntry(channel, message);
          try
          {
            client_.send(chatEntry);
          }
          catch (Exception ex)
          {
            System.err.println("Couldn't send message.");
            ex.printStackTrace();
          }

          messageText.selectAll();
        }
      });
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
