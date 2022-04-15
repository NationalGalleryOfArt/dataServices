package gov.nga.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Collator;
import java.text.Normalizer;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {

    public static Collator defaultCollator = Collator.getInstance();
    
    private static final Logger logger = LoggerFactory
            .getLogger(StringUtils.class);

    /*
     * public static class DiacriticComparer implements Comparator<String> {
     * public int compare(String o1, String o2) { return
     * defaultCollator.compare(o1, o2); } }
     */

    public static Collator getDefaultCollator() {
        return defaultCollator;
    }

    public static List<String> stringToList(String s) {
        List<String> l = CollectionUtils.newArrayList();
        l.add(s);
        return l;
    }
    
    // replaces high ASCI characters like the copyright and trademark symbols
    // from a string - Vadym's team uses this to transform names that are
    // subsequently used for storing nodes in the repository
    public static String removeHighASCIICharacters(String s) {
        return s.replaceAll("[^\\x00-\\x7F]", "");
    }

    public static String removeDiacritics(String in) {
    	if (in == null)
    		return null;
        return Normalizer.normalize(in, Normalizer.Form.NFD).replaceAll(
                "\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static String trimToMatchSize(String source, String target) {
        if (source != null && target != null
                && source.length() > target.length())
            source = source.substring(0, target.length());
        return source;
    }

    // put the finishing touches on the SQL of a parameterized prepared
    // statement
    public static String fillQueryParams(int size) {
        if (size > 0) {
            return org.apache.commons.lang.StringUtils.repeat("?,", size - 1)
                    + "?";
        } else
            return "";
    }

    public static boolean isNullOrEmpty(String str) {
        boolean res = true;

        try {

            if (str == null) {
                return res;
            }

            if (str.trim().equals("")) {
                return res;
            }

            res = false;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return res;
        }
        return res;
    }
    

    /*
     * This method returns true if p_str1 and p_str2 are identical strings 
     * in case diacritics are normalized. Case insensitive. 
     * It returns false otherwise
     * Example: p_str1 - Durer
     * p_str2 - dürer
     * Result: true
     */
    public static boolean isDiacriticalFormOf (String p_str1, String p_str2) {       
        
          p_str1 = removeDiacritics(p_str1.trim());
          p_str2 = removeDiacritics(p_str2.trim());
          
          return p_str1.equalsIgnoreCase(p_str2);
        
    }

    // This method returns size first tokens from the source string
    // given the delimiter provided
    // Example getSubstr("Hello my world! The life is good!", " ", 3) will
    // return "Hello my world! "
    // Please note - the newDelim will be added at the end of each token in the
    // result string
    public static String getSubstr(String source, String delim, int size,
            String newDelim) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer tt = new StringTokenizer(source, delim);

        if (newDelim == null) {
            newDelim = delim;
        }

        for (int i = 0; i < size; i++) {
            if (tt.hasMoreElements()) {
                buf.append(tt.nextToken());
                buf.append(newDelim);
            }
        }

        String temp = buf.toString();

        if (buf.lastIndexOf(",") > 0) {

            temp = buf.substring(0, buf.lastIndexOf(","));
            temp += " ...";

        }

        return temp;

    }

    // ummm... ok... you know... there are libraries available that do all this
    // already so why reinvent the wheel not to mention introduce a bunch of bugs
    // and inefficiencies. 
    public final static String htmlEntities[] = { "&euro;", "", "&lsquor;",
            "&fnof;", "&ldquor;", "&hellip;", "&dagger;", "&Dagger;", "&#710;",
            "&permil;", "&Scaron;", "&lsaquo;", "&OElig;", "", "&#381;", "",
            "", "&lsquo;", "&rsquo;", "&ldquo;", "&rdquo;", "&bull;",
            "&ndash;", "&mdash;", "&tilde;", "&trade;", "&scaron;", "&rsaquo;",
            "&oelig;", "", "&#382;", "&Yuml;", "&nbsp;", "&iexcl;", "&cent;",
            "&pound;", "&curren;", "&yen;", "&brvbar;", "&sect;", "&uml;",
            "&copy;", "&ordf;", "&laquo;", "&not;", "&shy;", "&reg;", "&macr;",
            "&deg;", "&plusmn;", "&sup2;", "&sup3;", "&acute;", "&micro;",
            "&para;", "&middot;", "&cedil;", "&sup1;", "&ordm;", "&raquo;",
            "&frac14;", "&frac12;", "&frac34;", "&iquest;", "&Agrave;",
            "&Aacute;", "&Acirc;", "&Atilde;", "&Auml;", "&Aring;", "&AElig;",
            "&Ccedil;", "&Egrave;", "&Eacute;", "&Ecirc;", "&Euml;",
            "&Igrave;", "&Iacute;", "&Icirc;", "&Iuml;", "&ETH;", "&Ntilde;",
            "&Ograve;", "&Oacute;", "&Ocirc;", "&Otilde;", "&Ouml;", "&times;",
            "&Oslash;", "&Ugrave;", "&Uacute;", "&Ucirc;", "&Uuml;",
            "&Yacute;", "&THORN;", "&szlig;", "&agrave;", "&aacute;",
            "&acirc;", "&atilde;", "&auml;", "&aring;", "&aelig;", "&ccedil;",
            "&egrave;", "&eacute;", "&ecirc;", "&euml;", "&igrave;",
            "&iacute;", "&icirc;", "&iuml;", "&eth;", "&ntilde;", "&ograve;",
            "&oacute;", "&ocirc;", "&otilde;", "&ouml;", "&divide;",
            "&oslash;", "&ugrave;", "&uacute;", "&ucirc;", "&uuml;",
            "&yacute;", "&thorn;", "&yuml;" };

    public final static String entities[] = { "F6", "E4", "FC", "D6", "C4",
            "DC", "DF", "3F", "5C", "2C", "3A", "3B", "23", "2B", "7E", "21",
            "22", "A7", "24", "25", "26", "28", "29", "3D", "3C", "3E", "7B",
            "5B", "5D", "7D", "2F", "E2", "EA", "EE", "F4", "FB", "C2", "CA",
            "CE", "D4", "DB", "E1", "E9", "ED", "F3", "FA", "C1", "C9", "CD",
            "D3", "DA", "E0", "E8", "EC", "F2", "F9", "C1", "C9", "CD", "D3",
            "DA", "B0", "B3", "B2", "80", "7C", "5E", "60", "B4", "27", "20",
            "40", "98", "2A" };

    public final static String charsHtml[] = { "ö", "ä", "ü", "Ö", "Ä", "Ü",
            "ß", "?", "\\", ",", ":", ";", "#", "+", "&tilde;", "!", "\"",
            "&sect;", "$", "%", "&amp;", "(", ")", "=", "&lt;", "&gt;", "{",
            "[", "]", "}", "/", "&acirc;", "&ecirc;", "&icirc;", "&ocirc;",
            "&ucirc;", "&Acirc;", "&Ecirc;", "&Icirc;", "&Ocirc;", "&Ucirc;",
            "&aacute;", "&eacute;", "&iacute;", "&oacute;", "&uacute;",
            "&Aacute;", "&Eacute;", "&Iacute;", "&Oacute;", "&Uacute;",
            "&agrave;", "&egrave;", "&igrave;", "&ograve;", "&Ugrave;",
            "&Agrave;", "&Egrave;", "&Igrave;", "&Ograve;", "&Ugrave;",
            "&deg;", "&sup3;", "&sup2;", "&euro;", "|", "&circ;", "`",
            "&acute;", "'", " ", "@", "~", "*" };

    public final static String chars[] = { "ö", "ä", "ü", "Ö", "Ä", "Ü", "ß",
            "?", "\\", ",", ":", ";", "#", "+", "~", "!", "\"", "§", "$", "%",
            "&", "(", ")", "=", "<", ">", "{", "[", "]", "}", "/", "â", "ê",
            "î", "ô", "û", "Â", "Ê", "Î", "Ô", "Û", "á", "é", "í", "ó", "ú",
            "Á", "É", "Í", "Ó", "Ú", "à", "è", "ì", "ò", "ù", "Á", "É", "Í",
            "Ó", "Ú", "°", "³", "²", "€", "|", "^", "`", "´", "'", " ", "@",
            "~", "*" };

    public static String entityToChar(String raw) {
        return (entityTo(raw, chars));
    }

    public static String entityToHtml(String raw) {
        String tempStr = charToHtml(entityTo(raw, chars));

        tempStr = tempStr.replaceAll("&acirc;&#8364;&#8482;", "'").replaceAll(
                "&Atilde;&copy;", "e").replaceAll("&acirc;&#8364;&#339;", "'")
                .replaceAll("&acirc;&#8364;&#65533;", "'").replaceAll(
                        "&acirc;&#8364;&#8221;", "-").replaceAll(
                        "&acirc;&#8364;&#8220;", "-");

        return tempStr;
    }

    public static String htmlToChar(String raw) {
        return convert(raw, charsHtml, chars);
    }

    public static String charToHtml(String raw) {
        if (raw == null)
            return null;
        char[] chars = raw.toCharArray();
        StringBuffer encoded = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '<')
                encoded.append("&lt;");
            else if (c == '>')
                encoded.append("&gt;");
            else if (c < 128)
                encoded.append(c);
            else if (c < 256)
                encoded.append(htmlEntities[c - 128]);
            else {
                encoded.append("&#");
                encoded.append((int) c);
                encoded.append(";");
            }
        }
        return encoded.toString();
    }

    public static String entityTo(String raw, String[] tc) {
        StringBuffer sb = new StringBuffer();
        boolean entity = false;
        raw = raw.replace('+', ' ');
        String tokens = tc == charsHtml ? "%<>" : "%";
        for (StringTokenizer st = new StringTokenizer(raw, tokens, true); st
                .hasMoreTokens();) {
            String token = st.nextToken();
            if (entity) {
                boolean replaced = false;
                for (int i = 0; i < entities.length; i++) {
                    if (token.startsWith(entities[i])) {
                        sb.append(tc[i]);
                        sb.append(token.substring(2));
                        replaced = true;
                        break;
                    }
                }
                if (!replaced)
                    sb.append(token);

                entity = false;
            } else if (token.equals("%")) {
                entity = true;
                continue;
            } else if (token.equals("<")) {
                sb.append("&lt;");
            } else if (token.equals(">")) {
                sb.append("&gt;");
            } else {
                sb.append(token);
            }
        }
        return (sb.toString());
    }

    public static String convert(String raw, String[] from, String[] to) {
        String result = raw;
        for (int i = 0; i < from.length; i++) {
            int idx = result.indexOf(from[i]);
            if (idx < 0)
                continue;
            StringBuffer sb = new StringBuffer();
            while (idx > -1) {
                sb.append(result.substring(0, idx));
                sb.append(to[i]);
                result = result.substring(idx + from[i].length());
                idx = result.indexOf(from[i]);
            }
            sb.append(result);
            result = sb.toString();
        }
        return result;
    }

    /*
     * This method removes international characters
     */
    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str,
                Normalizer.Form.NFD);
        // dpb wonders why you want to compile this pattern every time the method is called??? and why you didn't just use the 
        // existing "removeDiacritics()" method...
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
    
    public static String removeOnlyHTML(String htmlString) {
    	if (htmlString == null)
    		return null;
    	return htmlString.replaceAll("\\<.*?\\>", "");
    }

    /*
     * This method removes html tags & diacritics from the given string
     */
    public static String removeHTML(String htmlString) {
        return removeHTML(htmlString, true);
    }
    
    public static String removeHTML(String htmlString, final boolean removeDiacritics) {
        if (htmlString==null)
            return null;
        
        String toUnescape = htmlString.replaceAll("\\<.*?\\>", "").replaceAll("\r|\n|\"", " ");
        
        return removeDiacritics ?
                StringEscapeUtils.unescapeHtml(deAccent(toUnescape)) : StringEscapeUtils.unescapeHtml(toUnescape);
    }

    /*
     * This method strips first paragraph tag from the sting (if exists)
     */
    public static String stripParagraph(String htmlString) {
        String noParString = htmlString;
        
        if (noParString.startsWith("<p>")) {
        	noParString = noParString.replaceFirst("<p>", "");
        	if (noParString.endsWith("</p>")) {
        		noParString = noParString.substring(0, noParString.length()-4);
        	}
        }
        
        return noParString;
        
    }

    /**
     * In some cases &lt;p&gt; tag is not allowed within other tags (for example, within spans)
     * This methods removed all paragraphs from given Html string;
     * @param text input HTML string
     * @return html string with all paragraphs removed 
     */
    public static String stripAllParagraphs(String text){
        if (org.apache.commons.lang.StringUtils.isBlank(text)) return text;
        text = text.replaceAll("(?i)</?p.*?>", "");
        return text;
        
    }


    public static String encodeURIComponent(String comp)
    {
        String result = "";
        try
        {
            result = URLEncoder.encode(comp, "utf-8")
                           .replaceAll("\\+", "%20")
                           .replaceAll("\\%21", "!")
                           .replaceAll("\\%27", "'")
                           .replaceAll("\\%28", "(")
                           .replaceAll("\\%29", ")")
                           .replaceAll("\\%7E", "~");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error("Error converting string: " + comp, e);
        }
        return result;
    }
    
    public static String concatIfNotNull(String concatChars, String... list) {
        String result = "";
        for (String s : list) {
            if (s != null && s.length() > 0) {
                if (result.length() > 0)
                    result += concatChars;
                result += s;
            }
        }
        return result;
    }
    
     public static String firstNCharsOf(String source, int maxLen) {
        if (source != null && source.length() > maxLen)
            return source.substring(0, maxLen-1);
        return source;
    }

    /**
     * For now we replace only &lt;em&gt; and &lt;i&gt; tags with underscores
     * The rest of HTML markup passes through
     * @param text text to process
     * @return text with &lt;em&gt; and &lt;i&gt; tags replaced with underscores
     */
    public static String htmlToMarkdown(String text)
    {
        if (org.apache.commons.lang.StringUtils.isBlank(text)) 
        	return text;
        text = text.replaceAll("(?i)</?em.*?>", "_");
        text = text.replaceAll("(?i)</?i.*?>", "_");
        if (text != null)
        	text = text.trim();
        return text;
    }

    /**
     * ensure valid html fragment
     * @param text - html to sanitize
     * @return - sanitized html
     */
    public static String sanitizeHtml(String text)
    {
        if (org.apache.commons.lang.StringUtils.isBlank(text)) return text;

        Document doc = Jsoup.parseBodyFragment(text);
        return doc.body().html();
    }

    public static String markdownToHtml(String text)
    {
        if (org.apache.commons.lang.StringUtils.isBlank(text) || !text.contains("_")) return text;

        Pattern pattern = Pattern.compile("(_{2,})");
        Matcher matcher = pattern.matcher(text);
        StringBuffer tmp = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(tmp, org.apache.commons.lang.StringUtils.repeat("&lowbar;", matcher.group(1).length()));

        matcher.appendTail(tmp);
        text = tmp.toString();

        StringBuilder result = new StringBuilder();
        boolean closed = true;
        String[] parts = text.split("_");
        for (int i=0; i<parts.length-1; i++ )
        {
            result.append(parts[i]).append(closed ? "<em>" : "</em>");
            closed = !closed;
        }
        result.append(parts[parts.length-1]);
        if (!closed) result.append("</em>");
        return result.toString();

    }

    public static String markdownToText(String text)
    {
        if (org.apache.commons.lang.StringUtils.isBlank(text)) 
        	return text;
        return text.replaceAll("_","");
    }

    public static String removeOnlyHTMLAndFormatting(String text)
    {
        return removeOnlyHTML(markdownToText(text));
    }
    
    public static String removeMarkup(String text)
    {
        return removeHTML(markdownToText(text));
    }
    
    public static String removeMarkup(String text, final boolean removeDiacritics)
    {
        return removeHTML(text, removeDiacritics);
    }
    
    
}