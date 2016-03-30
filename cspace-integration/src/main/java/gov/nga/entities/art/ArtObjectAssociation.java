package gov.nga.entities.art;

import gov.nga.utils.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtObjectAssociation {
	
	private static final Logger log = LoggerFactory.getLogger(ArtObjectAssociation.class);
	
	public static enum ARTOBJECTDIVISIBILITY {
		ALL("all"),
		INSEPARABLE("inseparable"),
		SEPARABLE("separable");

		private String dataLabel = null;
		private ARTOBJECTDIVISIBILITY(String dataLabel) {
			this.dataLabel = dataLabel;
		}

		public String getDataLabel() {
			return dataLabel;
		}

		public static ARTOBJECTDIVISIBILITY fromLabel(String dataLabel) {
			if (INSEPARABLE.getDataLabel().equals(dataLabel))
				return INSEPARABLE;
			if (SEPARABLE.getDataLabel().equals(dataLabel))
				return SEPARABLE;
			if (ALL.getDataLabel().equals(dataLabel))
				return ALL;
			log.error("Unexpected value encountered for art object divisibility:" + dataLabel);
			return null;
		}
	}

	public static enum ARTOBJECTRELATIONSHIPROLE {
		PARENT,
		CHILD,
		SIBLING;
	}
	
	protected ArtObjectAssociation(ARTOBJECTDIVISIBILITY divisibility, ARTOBJECTRELATIONSHIPROLE roleOfReferenceArtObject, ArtObject associatedArtObject, ArtObject referenceArtObject) {
		setDivisibility(divisibility);
		setRoleOfReferenceArtObject(roleOfReferenceArtObject);
		setReferenceArtObject(referenceArtObject);
		setAssociatedArtObject(associatedArtObject);
	}

	private ARTOBJECTDIVISIBILITY divisibility = null;
	public ARTOBJECTDIVISIBILITY getDivisibility() {
		return divisibility;
	}
	private void setDivisibility(ARTOBJECTDIVISIBILITY divisibility) {
		this.divisibility = divisibility;
	}

	private ARTOBJECTRELATIONSHIPROLE roleOfReferenceArtObject = null;
	public ARTOBJECTRELATIONSHIPROLE getRoleOfReferenceArtObject() {
		return roleOfReferenceArtObject;
	}
	private void setRoleOfReferenceArtObject(ARTOBJECTRELATIONSHIPROLE roleOfReferenceArtObject) {
		this.roleOfReferenceArtObject = roleOfReferenceArtObject;
	}

	private ArtObject associatedArtObject = null;
	private void setAssociatedArtObject(ArtObject associatedArtObject) {
		this.associatedArtObject = associatedArtObject;
	}

	public ArtObject getAssociatedArtObject() {
		return associatedArtObject;
	}

	private ArtObject referenceArtObject = null;
	private void setReferenceArtObject(ArtObject referenceArtObject) {
		this.referenceArtObject = referenceArtObject;
	}

	public ArtObject getReferenceArtObject() {
		return referenceArtObject;
	}
	
	private static Map<ARTOBJECTDIVISIBILITY, Integer> defaultTypeSort = CollectionUtils.newHashMap(); 
	static {	
		defaultTypeSort.put(ARTOBJECTDIVISIBILITY.INSEPARABLE,0);
		defaultTypeSort.put(ARTOBJECTDIVISIBILITY.SEPARABLE,1);
	}

	private static Map<ARTOBJECTRELATIONSHIPROLE, Integer> defaultRoleSort = CollectionUtils.newHashMap();
	static {
		defaultRoleSort.put(ARTOBJECTRELATIONSHIPROLE.PARENT,0);
		defaultRoleSort.put(ARTOBJECTRELATIONSHIPROLE.CHILD,1);
		defaultRoleSort.put(ARTOBJECTRELATIONSHIPROLE.SIBLING,1);
	}

	public static Comparator<ArtObjectAssociation> sortByTypeRoleAccession = 
			new Comparator<ArtObjectAssociation>() {

		public int compare(ArtObjectAssociation a, ArtObjectAssociation b) {
			int typeCompare = defaultTypeSort.get(a.getDivisibility()).compareTo(defaultTypeSort.get(b.getDivisibility()));
			int roleCompare = defaultRoleSort.get(a.getRoleOfReferenceArtObject()).compareTo(defaultRoleSort.get(b.getRoleOfReferenceArtObject()));
			if (typeCompare == 0 && roleCompare == 0)
				return a.getAssociatedArtObject().getAccessionNum().compareTo(b.getAssociatedArtObject().getAccessionNum());
			return typeCompare == 0 ? roleCompare : typeCompare;
		}
	};
	
	public static Comparator<ArtObjectAssociation> defaultSorter = sortByTypeRoleAccession;


	static List<ArtObjectAssociation> filterByRole(
			List<ArtObjectAssociationRecord> associations, ArtObject referenceArtObject, ARTOBJECTRELATIONSHIPROLE role) {
		if (associations == null || referenceArtObject == null)
			return null;
		List<ArtObjectAssociation> list = CollectionUtils.newArrayList();
		for (ArtObjectAssociationRecord a : associations) {
			ArtObjectAssociation ctx = a.getAssociationContext(referenceArtObject);
			// only add the association to the list in cases where the role matches and in the special case of sibling relationship, the associated
			// art object is not the art object we're assessing the context of
			if ( ctx != null && ctx.getRoleOfReferenceArtObject().equals(role) && (!role.equals(ARTOBJECTRELATIONSHIPROLE.SIBLING) || !ctx.getAssociatedArtObject().equals(referenceArtObject)) )
				list.add(ctx);
		}
		return list;
	}
	
}
