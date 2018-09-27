/*
    NGA IIIF Authentication API Implementation: Simple Abstraction for Multiple Auth Services 

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

package gov.nga.api.iiif.auth;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IIIFAuthService {

	/*
	 *	  "service" : {
		    "@context": "http://iiif.io/api/auth/1/context.json",
		    "@id": "https://authentication.example.org/login",
		    "profile": "http://iiif.io/api/auth/1/login",
		    "label": "Login to Example Institution",
		    "service" : [
		      {
		        "@id": "https://authentication.example.org/token",
		        "profile": "http://iiif.io/api/auth/1/token"
		      },
		      {
		        "@id": "https://authentication.example.org/logout",
		        "profile": "http://iiif.io/api/auth/1/logout",
		        "label": "Logout from Example Institution"
		      }
		    ]
		  }
	 * 
	 */
	
	// set by IIIF auth API version 1 
	@JsonProperty("@context")
	String context = "http://iiif.io/api/auth/1/context.json";
	
	// the URL of the internal authentication service that produces an authorization token, e.g. "https://token.nga.gov/token";
	@JsonProperty("@id")
	String id = null;

	// the profile of this particular service
	String profile = null;
	
	// label that should be used by client applications in prompts regarding this service
	String label = null;
	
	// a list of sub-services, each of which might also support different auth mechanisms
	ArrayList<IIIFAuthService> service = null;
	
	public IIIFAuthService(String id, String profile, String label) {
		this.id = id;
		this.profile = profile;
		this.label = label;
	}
	
	public void addService(String id, String profile, String label) {
		IIIFAuthService s = new IIIFAuthService(id, profile, label);
		if (service == null)
			service = new ArrayList<IIIFAuthService>();
		service.add(s);
	}
	

}
