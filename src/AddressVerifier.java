import javax.swing.*;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressVerifier extends InputVerifier
{
  private static final Pattern IPV4_PATTERN = Pattern.compile(
    "^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}"+
    "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");

  @Override
  public boolean verify(JComponent component)
  {
    String text = ((JTextComponent)component).getText();
    if (StringUtil.isNullOrEmpty(text))
    {
      return true;
    }

    Matcher ipMatcher = IPV4_PATTERN.matcher(text);
    return ipMatcher.matches();
  }
}
