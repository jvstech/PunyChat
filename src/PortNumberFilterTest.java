public class PortNumberFilterTest implements FilterTest
{
  @Override
  public boolean test(String text)
  {
    try
    {
      if (StringUtil.isNullOrEmpty(text))
      {
        // allow empty strings
        return true;
      }

      int portNum = Integer.parseInt(text);
      if (portNum >= 1 && portNum <= 65535)
      {
        return true;
      }

      return false;
    }
    catch (NumberFormatException ex)
    {
      return false;
    }
  }
}
