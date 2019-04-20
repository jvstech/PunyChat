import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class GeneralizedFilter extends DocumentFilter
{
  private FilterTest filterTest_;

  public GeneralizedFilter(FilterTest filterTest)
  {
    filterTest_ = filterTest;
  }

  @Override
  public void insertString(FilterBypass filterBypass, int offset, String string,
    AttributeSet attributeSet)
    throws BadLocationException
  {
    Document doc = filterBypass.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.insert(offset, string);
    if (filterTest_.test(sb.toString()))
    {
      super.insertString(filterBypass, offset, string, attributeSet);
    }
  }

  @Override
  public void replace(FilterBypass filterBypass, int offset, int length,
                      String text, AttributeSet attributeSet)
    throws BadLocationException
  {
    Document doc = filterBypass.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.replace(offset, offset + length, text);
    if (filterTest_.test(sb.toString()))
    {
      super.replace(filterBypass, offset, length, text, attributeSet);
    }
  }

  @Override
  public void remove(FilterBypass filterBypass, int offset, int length)
    throws BadLocationException
  {
    Document doc = filterBypass.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.delete(offset, offset + length);
    if (filterTest_.test(sb.toString()))
    {
      super.remove(filterBypass, offset, length);
    }
  }
}
