import javax.swing.*;
import java.awt.*;

public class ChatUIFrame extends JFrame
{
  private JList<Channel> channelList;
  private JTextArea messagesText;
  private JTextField messageText;

  public ChatUIFrame()
  {
    initialize();
  }

  private void initialize()
  {
    try
    {
      setTitle("Puny Chat");
      setLayout(new GridBagLayout());
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
      contentPanel.add((channelList = new JList<>()), contentConstraints);
      // TODO: don't set this manually
      Channel[] channels = new Channel[]{ Channel.fromName("general") };
      channelList.setListData(channels);

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
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
