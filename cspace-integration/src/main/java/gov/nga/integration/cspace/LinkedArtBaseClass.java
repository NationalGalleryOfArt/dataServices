/*
    NGA ART DATA API: LinkedArtBaseClass is an astract class that provides some of the basic
    properties of a linked art data model entity 

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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "context", "id", "type" }) // context TBD
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class LinkedArtBaseClass extends Record {

	private String baseURL = null;
	private String context = null;
	private String type = null;
	private String label = null;
	private String value = null;
	
	public LinkedArtBaseClass(String type) {
		// namespace and source are not used in LinkedArt model 
		setNamespace(null);
		setSource(null);
		setType(type);
	}
	
	private void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	public String getContext() {
		return this.context;
	}
	
	public String getBaseUrl() {
		return this.baseURL;
	}

	void setBaseUrl(String baseURL) {
		this.baseURL = baseURL;
	}
	
	protected void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}

	protected void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;
	}


}


