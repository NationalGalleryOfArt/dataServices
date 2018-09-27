/*
    NGA IIIF Authentication API Implementation: simple info.json data object / container

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

import java.net.URI;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InfoJSON {
	@JsonProperty("@context")
	String context = null;

	@JsonProperty("@id")
	String id=null;

	String protocol = null;

	Integer width = null;
	Integer height = null;

	ArrayList<Size> sizes = null;
	String profile = null;

	IIIFAuthService service = null; 

	// populate context, id, protocol, width, height, sizes, and profile from an existing info.json
	public InfoJSON(URI InfoJsonURI, Integer maxWidth, Integer maxHeight) {

		/*
		JSONObject obj = new JSONObject(" .... ");
		String pageName = obj.getJSONObject("pageInfo").getString("pageName");

		JSONArray arr = obj.getJSONArray("posts");
		for (int i = 0; i < arr.length(); i++)
		{
			String post_id = arr.getJSONObject(i).getString("post_id");
			......
		}
		 */	
	}

	// example IIIF Auth Response
	/*
	{
		  "@context" : "http://iiif.io/api/image/2/context.json",
		  "@id" : "https://www.example.org/images/image1",
		  "protocol" : "http://iiif.io/api/image",
		  "width" : 600,
		  "height" : 400,
		  "sizes" : [
		    {"width" : 150, "height" : 100},
		    {"width" : 600, "height" : 400}
		  ],
		  "profile" : [
		    "http://iiif.io/api/image/2/level2.json",
		    {
		      "formats" : [ "gif", "pdf" ],
		      "qualities" : [ "color", "gray" ],
		      "supports" : [
		          "canonicalLinkHeader", "rotationArbitrary"
		      ]
		    }
		  ],
		  "service" : {
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
		}
	 */
}
