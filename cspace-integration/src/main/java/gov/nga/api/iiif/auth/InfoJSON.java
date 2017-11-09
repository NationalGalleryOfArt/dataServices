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
