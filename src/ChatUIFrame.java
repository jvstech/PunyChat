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
      rootConstraints.gridx = 0;
      rootConstraints.gridy = 0;

      JPanel buttonPanel;
      add((buttonPanel = new JPanel()), rootConstraints);

      rootConstraints.gridy++;
      rootConstraints.fill = GridBagConstraints.VERTICAL;
      JPanel contentPanel;
      add((contentPanel = new JPanel()), rootConstraints);

      buttonPanel.setLayout(new FlowLayout());

      GridBagConstraints contentConstraints = new GridBagConstraints();
      contentConstraints.gridx = 0;
      contentConstraints.gridy = 0;
      contentPanel.add((channelList = new JList<>()), contentConstraints);
      Channel[] channels = new Channel[]{ Channel.fromName("general") };
      channelList.setListData(channels);

      contentConstraints.gridx++;
      contentConstraints.weightx = 1;
      contentConstraints.fill = GridBagConstraints.HORIZONTAL;
      JPanel chatPanel;
      contentPanel.add((chatPanel = new JPanel()), contentConstraints);
      chatPanel.setLayout(new GridBagLayout());

      GridBagConstraints chatConstraints = new GridBagConstraints();
      chatConstraints.gridx = 0;
      chatConstraints.gridy = 0;
      chatConstraints.weighty = 1;
      chatConstraints.fill = GridBagConstraints.REMAINDER;
      chatPanel.add((messagesText = new JTextArea()), chatConstraints);

      chatConstraints.gridy++;
      chatConstraints.weighty = 0;
      chatConstraints.fill = GridBagConstraints.NONE;
      chatPanel.add((messageText = new JTextField(80)), chatConstraints);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
