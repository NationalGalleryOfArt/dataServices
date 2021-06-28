/*
    Utils: CaseInsensitiveSubstringMatcher implements a case insensitive string matcher
  
    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: NGA Contractors

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package gov.nga.utils;

import org.hamcrest.Description;
//import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CaseInsensitiveSubstringMatcher extends TypeSafeMatcher<String> {

    private final String subString;

    private CaseInsensitiveSubstringMatcher(final String subString) {
        this.subString = subString;
    }

    @Override
    protected boolean matchesSafely(final String actualString) {
        return actualString.toLowerCase().contains(this.subString.toLowerCase());
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("containing substring \"" + this.subString + "\"");
    }

    public static Matcher<String> containsStringCaseInsensitive(final String subString) {
        return new CaseInsensitiveSubstringMatcher(subString);
    }
}