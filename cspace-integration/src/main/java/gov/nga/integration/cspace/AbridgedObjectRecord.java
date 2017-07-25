package gov.nga.integration.cspace;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectAssociation;
import gov.nga.entities.art.ArtObjectConstituent;
import gov.nga.entities.art.ArtObjectImage;
import gov.nga.integration.cspace.imageproviders.WebImage;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;

// SEE notes in ObjectRecord for notes about alignment of this representation with Sirma's CS integration services implementation

@JsonPropertyOrder({ "namespace", "source", "id", "accessionNum", "title", "classification", "artistNames", "lastModified", "references" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbridgedObjectRecord extends Record implements NamespaceInterface {

	private static final Logger log = LoggerFactory.getLogger(AbridgedObjectRecord.class);
	
	private static final String defaultNamespace = "cultObj";

	// mandatory fields of the API - source and id are also mandatory and are inherited from the base object Record
    private String accessionNum;				// mandatory field
	private String title;						// optional field, but searchable, so including it in abridged object used in search results
	private String classification;				// mandatory field for cspace
    private String artistNames;					// mandatory field for cspace
    
    private static boolean testmode = false;

	public enum PREDICATE {
		HASPARENT("hasParent"),
		HASCHILD("hasChild"),
		HASSIBLING("hasSibling");

		private String label;
		public String getLabel() {
			if (testmode)
				return "partner2" + label;
			return label;
		}

		private PREDICATE(String label) {
			this.label = label;
		};
	};

	public AbridgedObjectRecord(ArtObject o, CSpaceTestModeService ts) throws InterruptedException, ExecutionException {
		this(o, false, ts, null);
	}
	
	public AbridgedObjectRecord(ArtObject o, boolean references, CSpaceTestModeService ts, List<CSpaceImage> images) throws InterruptedException, ExecutionException {
		if (o == null)
			return;

		testmode = ts.isTestModeOtherHalfObjects();

		if (references)
			setReferences(o, ts, images);
		setNamespace(defaultNamespace);
		setSource("tms");
		setId(o.getObjectID().toString());
        setClassification(o.getClassification());
		setAccessionNum(o.getAccessionNum());
        setTitle(StringUtils.removeOnlyHTMLAndFormatting(o.getTitle()));
        setLastModified(o.getLastDetectedModification());

        this.artistNames = constituentNames(o.getArtists(),"artist");
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
		if (classification != null) {
			if ( classification.startsWith("Index of American Design") )
				classification = "Drawing";
			else if ( classification.startsWith("Ephemera") )
				classification = "Ephemera";
		}
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

	public String constituentNames(List<ArtObjectConstituent> constituents, String filterRoleType) {
		String cNames=null;
		if (constituents != null) {
			for (ArtObjectConstituent aoc : constituents) {
				String cName = aoc.getConstituent().getForwardDisplayName();
				String roleType = aoc.getRoleType();
				String role = aoc.getRole();
				log.trace("Role: " + role);
				log.trace("Role type: " + roleType);
				/* DCL has discussed limiting the roletypes and / or roles returned by artistNames - personally I think that's a mistake but
				 * the jury is still out
				 */
				/* if ( cName != null && !StringUtils.isNullOrEmpty(roleType) && roleType.toLowerCase().equals("artist") 
					 && !StringUtils.isNullOrEmpty(role) && role.toLowerCase().equals("artist")) {
					log.info("HERE HER HE H");
					cNames += cName;
					if (cNames != null)
						cNames += "; ";
					else
						cNames = "";
					
				 */	
				if ( cName != null && !StringUtils.isNullOrEmpty(roleType) && roleType.toLowerCase().equals(filterRoleType) 
						&& !StringUtils.isNullOrEmpty(role) ) {
					if (cNames != null)
						cNames += "; ";
					else
						cNames = "";
					cNames += cName;
					if (role != null)
						cNames += " (" + role + ")"; 
				}
			}
		}
		return cNames;
	}
	
	public void setReferences(ArtObject o, CSpaceTestModeService ts, List<CSpaceImage> images) throws InterruptedException, ExecutionException {
		List<Reference> rList = CollectionUtils.newArrayList();
		// first we go through the related objects
		ArtObjectAssociation aop = o.getParentAssociation();
		if (aop != null) {
			AbridgedObjectRecord aor = new AbridgedObjectRecord(aop.getAssociatedArtObject(), false, ts, images);
			rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASPARENT.getLabel(), aor));
		}
		List<ArtObjectAssociation> l = o.getChildAssociations();
		if (l != null) {
			for (ArtObjectAssociation aoc : l) {
				if (aoc != null) {
					AbridgedObjectRecord aor = new AbridgedObjectRecord(aoc.getAssociatedArtObject(), false, ts, images);
					rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASCHILD.getLabel(), aor));
				}
			}
		}
		l = o.getSiblingAssociations();
		if (l!= null) {
			for (ArtObjectAssociation aos : l) {
				if (aos != null) {
					AbridgedObjectRecord aor = new AbridgedObjectRecord(aos.getAssociatedArtObject(), false, ts, images);
					rList.add(new Reference(AbridgedObjectRecord.PREDICATE.HASSIBLING.getLabel(), aor));
				}
			}		
		}
		
		// iterate through the images associated with this art object
		if (images != null) {
			for (CSpaceImage ci : images) {
				WebImage wi = WebImage.factory(ci, ts);
				AbridgedImageRecord air = new AbridgedImageRecord(wi,false,ts);
				if (ArtObjectImage.isPrimaryView(wi)) {
					rList.add(new Reference(AbridgedImageRecord.PREDICATE.HASPRIMARYDEPICTION.getLabel(), air));
				}
				// we don't want to return cropped images associated with this object
				else if (!wi.isCropped()) {
					rList.add(new Reference(AbridgedImageRecord.PREDICATE.HASDEPICTION.getLabel(), air));
				}
			}
		}

		if (rList.size() <= 0)
			rList = null;

		setReferences(rList);
	}

}

 