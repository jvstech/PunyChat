public class MaxSizeFilterTest implements FilterTest
{
  private int maxSize_ = 0;

  public MaxSizeFilterTest(int maxSize)
  {
    setMaxSize(maxSize);
  }

  public int getMaxSize()
  {
    return maxSize_;
  }

  public void setMaxSize(int maxSize)
  {
    if (maxSize < 0)
    {
      maxSize = 0;
    }

    maxSize_ = maxSize;
  }

  @Override
  public boolean test(String text)
  {
    if (maxSize_ == 0)
    {
      return true;
    }

    if (StringUtil.isNullOrEmpty(text))
    {
      return true;
    }

    return (text.length() <= maxSize_);
  }
}
