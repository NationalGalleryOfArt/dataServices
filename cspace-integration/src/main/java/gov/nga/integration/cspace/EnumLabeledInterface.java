/*
    NGA ART DATA API: EnumLabeledInterface provides an interface and some static methods
    that can be helpful to eliminate duplicated code in enums that carry labels in addition
    to their default labels - enum.toString().

    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

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
package gov.nga.integration.cspace;

import java.util.Map;

import gov.nga.utils.CollectionUtils;

public interface EnumLabeledInterface {
	
	// using a map, we can change the contents of the map and use
	// it in a static context to store all of our enum values, indexed by the name
	// of the class and the value of the enum
	static Map<String,String[]> labels = CollectionUtils.newHashMap();

	static String makeKey(String className, String enumValue) {
		return className + "." + enumValue;
	}
	
	static void setLabels(Boolean testMode, String className, String enumValue, String[] labels) {
		// modify the label if we're running in test mode so that multitenancy can be tested
		// in consuming applications requiring support for multitenancy, e.g. conservation space
		// this isn't critical for the NGA except when testing multitenancy with conservation space
		if ( testMode ) {
			for (int j=0; j<labels.length; j++)
				labels[j] = "partner2" + labels;
		}
		EnumLabeledInterface.labels.put(makeKey(className, enumValue), labels);
	}
	
	static String[] getLabels(String className, String enumValue) {
		
		return labels.get(makeKey(className, enumValue));
	}

}