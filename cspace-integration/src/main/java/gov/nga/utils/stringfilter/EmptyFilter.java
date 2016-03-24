package gov.nga.utils.stringfilter;

public class EmptyFilter implements StringFilter
{
    @Override
    public String getFilteredString(String s)
    {
        return s;
    }
}
