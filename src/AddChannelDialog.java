import javax.swing.*;
import java.awt.*;

public class AddChannelDialog extends JDialog
{
  public AddChannelDialog()
  {
    initializeDialog();
  }

  private void initializeDialog()
  {
    setTitle("Add Channel");
    setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.WEST;

    add(new JLabel("Channel name: "), constraints);
    constraints.gridy++;
    add(new JLabel("Custom password: "), constraints);
  }
}
