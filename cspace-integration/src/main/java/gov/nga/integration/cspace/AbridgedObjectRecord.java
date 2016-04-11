package gov.nga.integration.cspace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectAssociation;
import gov.nga.entities.art.ArtObjectConstituent;
import gov.nga.entities.art.ArtObjectImage;
import gov.nga.entities.art.Derivative;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;

@JsonPropertyOrder({ "namespace", "source", "id", "accessionNum", "title", "classification", "artistNames", "lastDetectedModification", "references" })
public class AbridgedObjectRecord extends Record implements NamespaceInterface {

	private static final String defaultNamespace = "cultObj";

	// mandatory fields of the API - source and id are also mandatory and are inherited from the base object Record
    private String accessionNum;				// optional, but searchable, so including it in results
	private String title;						// optional, but searchable, so including it in results
	private String classification;				// mandatory field for cspace
    private String artistNames;					// mandatory field for cspace
    private String lastDetectedModification;	// not defined at all in conspace, but searchable, so including it in results

	// if null, don't include it since we have an option in the search APIs to not include references at all - so we don't want to 
	// make it appear as if there are no references - we just won't include them at all if they are null
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<Reference> references;

	public enum PREDICATE {
		HASPARENT("hasParent"),
		HASCHILD("hasChild"),
		HASSIBLING("hasSibling");

		private String label;
		public String getLabel() {
			return label;
		}

		private PREDICATE(String label) {
			this.label = label;
		};
	};

	public AbridgedObjectRecord(ArtObject o, boolean references) {
		if (o == null)
			return;
		
		if (references)
			setReferences(o);
		setNamespace(defaultNamespace);
		setSource("tms");
		setId(o.getObjectID().toString());
        setClassification(o.getClassification());
		setAccessionNum(o.getAccessionNum());
        setTitle(StringUtils.removeMarkup(o.getTitle()));
        setLastDetectedModification(o.getLastDetectedModification());

        this.artistNames = constituentNames(o.getArtists());
	}

	public static String getDefaultNamespace() {
		return defaultNamespace;
	}

	public String getArtistNames() {
		return artistNames;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAccessionNum() {
		return accessionNum;
	}

	public void setAccessionNum(String accessionNum) {
		this.accessionNum = accessionNum;
	}

	public String getLastDetectedModification() {
		return lastDetectedModification;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public void setLastDetectedModification(String lastDetectedModification) {
		this.lastDetectedModification = lastDetectedModification;
	}

	public String constituentNames(List<ArtObjectConstituent> constituents) {
		String cNames=null;
		if (constituents != null) {
			for (ArtObjectConstituent aoc : constituents) {
				String cName = aoc.getConstituent().getForwardDisplayName();
				String roleType = aoc.getRoleType();
				if (cName != null) {
					if (cNames != null)
						cNames += "; ";
					else
						cNames = "";
					cNames += cName;
					if (roleType != null)
						cNames += " (" + roleType + ")";
				}
			}
		}
		return cNames;
	}
	
	public void setReferences(ArtObject o) {

		List<Reference> rList = CollectionUtils.newArrayList();
		// first we go through the related objects
		ArtObjectAssociation aop = o.getParentAssociation();
		if (aop != null) {
			AbridgedObjectRecord aor = new AbridgedObjectRecord(aop.getAssociatedArtObject(),false);
			rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASPARENT.getLabel(), aor));
		}
		List<ArtObjectAssociation> l = o.getChildAssociations();
		if (l != null) {
			for (ArtObjectAssociation aoc : l) {
				if (aoc != null) {
					AbridgedObjectRecord aor = new AbridgedObjectRecord(aoc.getAssociatedArtObject(),false);
					rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASCHILD.getLabel(), aor));
				}
			}
		}
		l = o.getSiblingAssociations();
		if (l!= null) {
			for (ArtObjectAssociation aos : l) {
				if (aos != null) {
					AbridgedObjectRecord aor = new AbridgedObjectRecord(aos.getAssociatedArtObject(),false);
					rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASSIBLING.getLabel(), aor));
				}
			}		
		}
		// then we go through the largest images associated with this work and return them as associated images
		for (Derivative d : o.getLargestImages()) {
			AbridgedImageRecord air = new AbridgedImageRecord(d);
			if (ArtObjectImage.isPrimaryView(d)) {
				rList.add(new Reference(AbridgedImageRecord.PREDICATE.HASPRIMARYDEPICTION.getLabel(), air));
			}
			else {
				rList.add(new Reference(AbridgedImageRecord.PREDICATE.HASDEPICTION.getLabel(), air));
			}
		}

		if (rList.size() <= 0)
			rList = null;

		this.references = rList;
		//this.references = rList.toArray(new Reference[rList.size()]);
	}

}

 