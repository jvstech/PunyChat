import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

  public ChatUIFrame()
  {
    initialize();
  }

  public DefaultListModel<Channel> getChannelList()
  {
    return channelListModel;
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
          StringBuilder sb = new StringBuilder();
          for (ChatMessage message : chatMessages_.get(channel))
          {
            sb.append(message.toString() + "\n");
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
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
      });
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
