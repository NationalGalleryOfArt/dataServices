package gov.nga.utils.stringfilter;

import gov.nga.utils.StringUtils;

public class RemoveMarkupFilter implements StringFilter
{
    @Override
    public String getFilteredString(String s)
    {
        return StringUtils.removeMarkup(s);
    }
}
