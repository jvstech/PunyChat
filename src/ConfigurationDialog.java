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
  private JTextField addressText;
  private JRadioButton multicastButton;
  private JRadioButton directButton;
  private JRadioButton broadcastButton;
  private JCheckBox bindNetInterfaceCheckbox;
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
    contentPanel.add(new JLabel("Multicast/remote address: "), constraints);

    // Interface label
    constraints.gridy += 5;   // skip five rows for checkboxes
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

    // Address text box
    constraints.gridy++;
    contentPanel.add((addressText = new JTextField(30)), constraints);
    addressText.setText("225.225.8.0");
    addressText.setInputVerifier(new AddressVerifier());

    // Multicast mode radio button
    constraints.gridy++;
    contentPanel.add((multicastButton = new JRadioButton()), constraints);
    multicastButton.setText("Multicast mode");
    multicastButton.setSelected(true);
    multicastButton.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        bindNetInterfaceCheckbox.setEnabled(
          e.getStateChange() == ItemEvent.SELECTED);
        netInterfaceText.setEnabled(bindNetInterfaceCheckbox.isEnabled() &&
          bindNetInterfaceCheckbox.isSelected());
      }
    });

    // Direct mode radio button
    constraints.gridy++;
    contentPanel.add((directButton = new JRadioButton()), constraints);
    directButton.setText("Direct mode");

    // Broadcast mode radio button
    constraints.gridy++;
    contentPanel.add((broadcastButton = new JRadioButton()), constraints);
    broadcastButton.setText("Broadcast mode");
    broadcastButton.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        addressText.setEnabled(e.getStateChange() != ItemEvent.SELECTED);
      }
    });

    // Group the radio buttons together
    ButtonGroup modeGroup = new ButtonGroup();
    modeGroup.add(multicastButton);
    modeGroup.add(directButton);
    modeGroup.add(broadcastButton);

    // Bind network interface checkbox
    constraints.gridy++;
    contentPanel.add((bindNetInterfaceCheckbox = new JCheckBox()), constraints);
    bindNetInterfaceCheckbox.setText("Bind to specific network interface");
    bindNetInterfaceCheckbox.addItemListener(new ItemListener()
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
        if (broadcastButton.isSelected())
        {
          configuration_.setBroadcast();
        }
        else
        {
          try
          {
            configuration_.setAddress(InetAddress.getByName(
              addressText.getText()));
          }
          catch (UnknownHostException ex)
          {
            configuration_.setAddress(Configuration.DEFAULT_ADDRESS);
          }
        }

        if (bindNetInterfaceCheckbox.isSelected())
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
