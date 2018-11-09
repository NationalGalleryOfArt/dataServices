package gov.nga.integration.cspace;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObjectConstituent;
import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { 	
	"namespace", "source", "context", "id", "type", "carried_out_by"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductionRecord extends LinkedArtBaseClass {

	private static final String defaultNamespace = "Production";

	// mandatory fields of the API - source and id are also mandatory and are inherited from the base object Record
	private AbridgedObjectRecord artObjectRecord;

	public ProductionRecord(AbridgedObjectRecord artObjectRecord, String predicate)  {
		super(defaultNamespace);
		if (artObjectRecord != null) {
			setArtObjectRecord(artObjectRecord);
			setId(artObjectRecord.getUrl().toString().replaceAll("\\.json", "") + "/" + predicate);
		}
	}
	
	private void setArtObjectRecord(AbridgedObjectRecord artObjectRecord) {
		this.artObjectRecord = artObjectRecord;
	}

	public List<ActorRecord> getCarried_out_by() {
		List<ActorRecord> l = CollectionUtils.newArrayList();
		for ( ArtObjectConstituent oc : artObjectRecord.getArtObject().getArtists() ) {
			l.add(new ActorRecord(oc.getConstituent()));
		}
		return l;
	}
	
}

