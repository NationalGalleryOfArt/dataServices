// TODO - consider renaming to LinkedArtActors or creating a LinkedArt package and moving / renaming
// such classes into that package - there are too many classes in the cspace package at this point
/*
    NGA ART DATA API: ActorRecord provides a JSON bean container for Linked Art actors.

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.Constituent;

@JsonPropertyOrder( { 	
	"id", "type", "label", "source", "invertedLabel", "referred_to_by", "identified_by"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActorRecord extends LinkedArtRecord implements NamespaceInterface {

	//private static final String defaultNamespace = "Actor";

	// mandatory fields of the API - source and id are also mandatory and are inherited from the base object Record
	@JsonIgnore
	private Constituent constituent;
	
	public ActorRecord(Constituent constituent)  {
		super("Actor");
		if (constituent == null)
			return;
		
		setConstituent(constituent);
		
		//setNamespace(defaultNamespace);
		//setSource("tms");
		
		// displayDate is represented as a linguistic object because it's not "data" per se but
		// representational in nature / authored content and it includes "personal life events" and
		// nationality since it covers both nationality as well as a span of time dealing with the
		// lifetime (personal life events) of a Person
		addReferredToBy(
			new LinkedArtLinguisticObject("displayDate", constituent.getDisplayDate(),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300069671", "lifetime"),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404439", "dates (spans of time)"),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300379842", "nationality")
			)
		);

		addClassifiedAs(
			new LinkedArtType(constituent.getNationality(),null,
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300379842", "nationality")
			)
		);
		
		addIdentity(
			new LinkedArtName(constituent.getForwardDisplayName(),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404670", "preferred terms")
			)
		);

		addIdentity(
			new LinkedArtName(constituent.getPreferredDisplayName(),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404672", "inverted terms"),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404670", "preferred terms")
			)
		);
		
	}

	private void setConstituent(Constituent constituent) {
		this.constituent = constituent;
	}
	
	public Constituent getConstituent() {
		return constituent;
	}
	
	
	public String getType() {
		if ( constituent.getConstituentType().equals("individual") )
			return "Person";
		else if ( constituent.getConstituentType().equals("corporate") ||  
				  constituent.getConstituentType().equals("couple")    ||
				  constituent.getConstituentType().equals("loan_consortium") )
			return "Group";
		return "Actor";
	}
	
	// TODO - for each type of private entity, we need a way to detect within the
	// entity itself whether the entity is public or private and return an ID accordingly
	public String getId() {
		return "https://api.nga.gov/art/tms/actor/" + constituent.getConstituentID();
	}

	public String getLabel() {
		return constituent.getForwardDisplayName();
	}
	
	public TimeSpanContainer getBrought_into_existence_by() {
		Constituent c = getConstituent();
		if ( c != null ) {
			return new TimeSpanContainer(
					"brought_info_existence_by", 
					"BeginningOfExistence", 
					getId(),
					getSource(),
					new TimeSpan(c.getBeginYear(), c.getBeginYear())
			);
		}
		return null;
	}

	public TimeSpanContainer getTaken_out_of_existence_by() {
		Constituent c = getConstituent();
		if ( c != null ) {
			return new TimeSpanContainer(
					"taken_out_of_existence_by", 
					"EndOfExistence", 
					getId(),
					getSource(),
					new TimeSpan(c.getEndYear(), c.getEndYear())
			);
		}
		return null;
	}
	
}

