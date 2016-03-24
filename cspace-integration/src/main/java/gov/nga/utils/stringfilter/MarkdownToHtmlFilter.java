package gov.nga.utils.stringfilter;

import gov.nga.utils.StringUtils;

public class MarkdownToHtmlFilter implements StringFilter
{
    @Override
    public String getFilteredString(String s)
    {
        return StringUtils.markdownToHtml(s);
    }
}
