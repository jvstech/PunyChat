import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

public class ConfigurationDialog extends JDialog
{
  // components
  private JPanel contentPanel;
  private JPanel buttonPanel;
  private JTextField userNameText;
  private JTextField groupAddressText;
  private JCheckBox broadcastCheckbox;
  private JCheckBox bindNetInterfaceChecbox;
  private JComboBox<NetworkInterface> netInterfaceText;
  private JTextField portText;
  private JButton okButton;
  private JButton cancelButton;
  // resulting values
  private Configuration configuration_;
  private boolean cancelled_ = true;

  public ConfigurationDialog()
  {
    initializeDialog();
  }

  public boolean wasCancelled()
  {
    return cancelled_;
  }

  public Configuration getConfiguration()
  {
    return configuration_;
  }

  private void initializeDialog()
  {
    setTitle("Configuration");
    setBackground(new Color(60,63,65));
    setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    setLayout(new GridBagLayout());
    setLocationRelativeTo(null);

    GridBagConstraints rootConstraints = new GridBagConstraints();
    rootConstraints.gridx = 0;
    rootConstraints.gridy = 0;
    rootConstraints.fill = GridBagConstraints.VERTICAL;
    add((contentPanel = new JPanel(new GridBagLayout())), rootConstraints);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(4, 4, 4, 4);

    // User name label
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(new JLabel("User name: "), constraints);

    // Multicast address label
    constraints.gridy++;
    contentPanel.add(new JLabel("Multicast address: "), constraints);

    // Interface label
    constraints.gridy += 3;   // skip two rows for checkboxes
    contentPanel.add(new JLabel("Interface: "), constraints);

    // Port label
    constraints.gridy++;
    contentPanel.add(new JLabel("Port: "), constraints);

    // User name text box
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    contentPanel.add((userNameText = new JTextField(18)), constraints);
    ((PlainDocument)userNameText.getDocument()).setDocumentFilter(
      new GeneralizedFilter(new MaxSizeFilterTest(18)));
    userNameText.setText(System.getProperty("user.name"));

    // Multicast address text box
    constraints.gridy++;
    contentPanel.add((groupAddressText = new JTextField(30)), constraints);
    groupAddressText.setText("225.225.8.0");
    groupAddressText.setInputVerifier(new AddressVerifier());

    // Broadcast mode checkbox
    constraints.gridy++;
    contentPanel.add((broadcastCheckbox = new JCheckBox()), constraints);
    broadcastCheckbox.setText("Broadcast mode");
    broadcastCheckbox.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        groupAddressText.setEnabled(e.getStateChange() != ItemEvent.SELECTED);
      }
    });

    // Bind network interface checkbox
    constraints.gridy++;
    contentPanel.add((bindNetInterfaceChecbox = new JCheckBox()), constraints);
    bindNetInterfaceChecbox.setText("Bind to specific network interface");
    bindNetInterfaceChecbox.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        netInterfaceText.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });

    // Interface combo box
    constraints.gridy++;
    contentPanel.add((netInterfaceText = new JComboBox<>()), constraints);
    netInterfaceText.setEnabled(false);
    for (NetworkInterface netInt : ChatClient.getValidInterfaces())
    {
      netInterfaceText.addItem(netInt);
    }

    // Port text box
    constraints.gridy++;
    contentPanel.add((portText = new JTextField(6)), constraints);
    portText.setText("64982");
    ((PlainDocument)portText.getDocument()).setDocumentFilter(
      new GeneralizedFilter(new PortNumberFilterTest()));

    rootConstraints.gridy++;
    add((buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))),
      rootConstraints);

    // OK button
    buttonPanel.add((okButton = new JButton("OK")));
    okButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancelled_ = false;
        configuration_ = new Configuration();
        configuration_.setUserName(userNameText.getText());
        configuration_.setPort(Integer.parseInt(portText.getText()));
        if (broadcastCheckbox.isSelected())
        {
          configuration_.setBroadcast(true);
        }
        else
        {
          try
          {
            configuration_.setAddress(InetAddress.getByName(
              groupAddressText.getText()));
          }
          catch (UnknownHostException ex)
          {
            configuration_.setAddress(Configuration.DEFAULT_ADDRESS);
          }
        }

        if (bindNetInterfaceChecbox.isSelected())
        {
          configuration_.setNetworkInterface(
            (NetworkInterface)netInterfaceText.getSelectedItem());
        }
        else
        {
          configuration_.setNetworkInterface(null);
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
