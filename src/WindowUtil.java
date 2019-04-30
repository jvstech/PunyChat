import javax.swing.*;
import java.awt.*;

public class WindowUtil
{
  private static Dimension screenSize_ =
    Toolkit.getDefaultToolkit().getScreenSize();
  public static void center(Window frame)
  {
    if (frame == null)
    {
      return;
    }

    // Center the frame
    frame.setLocation(screenSize_.width / 2 - frame.getSize().width / 2,
      screenSize_.height / 2 - frame.getSize().height / 2);
  }
}
