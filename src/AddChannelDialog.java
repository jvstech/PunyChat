import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class AddChannelDialog extends JDialog
{
  // UI components
  private JPanel contentPanel;
  private JPanel buttonPanel;
  private JTextField channelNameText;
  private JCheckBox useCustomPasswordCheck;
  private JPasswordField passwordText;
  private JButton okButton;
  private JButton cancelButton;

  // property fields
  private boolean cancelled_ = true;
  private Channel channel_ = null;

  public AddChannelDialog()
  {
    initializeDialog();
  }

  public Channel getChannel()
  {
    return channel_;
  }

  public boolean wasCancelled()
  {
    return cancelled_;
  }

  private void initializeDialog()
  {
    setTitle("Add Channel");
    setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    setLayout(new GridBagLayout());
    setLocationRelativeTo(null);

    GridBagConstraints rootConstraints = new GridBagConstraints();
    rootConstraints.insets = new Insets(4,4,4,4);

    // Content panel
    rootConstraints.gridx = 0;
    rootConstraints.gridy = 0;
    rootConstraints.fill = GridBagConstraints.VERTICAL;
    add((contentPanel = new JPanel()), rootConstraints);
    contentPanel.setLayout(new GridBagLayout());

    // Button panel
    rootConstraints.gridy++;
    rootConstraints.fill = GridBagConstraints.NONE;
    add((buttonPanel = new JPanel(new FlowLayout())), rootConstraints);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(4, 4, 4, 4);

    // Labels
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.WEST;

    contentPanel.add(new JLabel("Channel name: "), constraints);
    constraints.gridy += 2;
    contentPanel.add(new JLabel("Custom password: "), constraints);

    // Channel text box
    constraints.gridx++;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    contentPanel.add((channelNameText = new JTextField(20)), constraints);

    // Custom password check box
    constraints.gridy++;
    contentPanel.add((useCustomPasswordCheck = new JCheckBox()), constraints);
    useCustomPasswordCheck.setText("Use custom password");
    useCustomPasswordCheck.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        passwordText.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });

    // Password text box
    constraints.gridy++;
    contentPanel.add((passwordText = new JPasswordField(20)), constraints);
    passwordText.setEnabled(false);

    // OK button
    buttonPanel.add((okButton = new JButton("OK")));
    okButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (StringUtil.isNullOrEmpty(channelNameText.getText()))
        {
          JOptionPane.showMessageDialog(null,
            "You must enter a channel name.", "Channel Name",
            JOptionPane.ERROR_MESSAGE);
          channelNameText.grabFocus();
          return;
        }

        cancelled_ = false;
        if (useCustomPasswordCheck.isSelected() &&
          passwordText.getPassword().length > 0)
        {
          // This is bad security. Don't do this in any security-necessity
          // production code.
          String password = new String(passwordText.getPassword());
          channel_ = Channel.tryFromProtectedName(channelNameText.getText(),
            new String(passwordText.getPassword()));
        }
        else
        {
          channel_ = Channel.tryFromName(channelNameText.getText());
        }

        setVisible(false);
      }
    });

    // Cancel button
    buttonPanel.add((cancelButton = new JButton("Cancel")));
    cancelButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancelled_ = true;
        setVisible(false);
      }
    });
  }
}
