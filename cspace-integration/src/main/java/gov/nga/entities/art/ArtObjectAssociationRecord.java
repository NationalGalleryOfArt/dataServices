package gov.nga.entities.art;

import gov.nga.entities.art.ArtObjectAssociation.ARTOBJECTDIVISIBILITY;
import gov.nga.entities.art.ArtObjectAssociation.ARTOBJECTRELATIONSHIPROLE;
import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

class ArtObjectAssociationRecord extends ArtEntityImpl {

	protected static final String fetchAllArtObjectAssociationsQuery =  
			"SELECT aoa.fingerprint, aoa.associationID, " +
					"       aoa.parentObjectID, aoa.childObjectID, aoa.relationship " +  
					"FROM data.object_associations aoa "; 
	// "ORDER BY aoa.associationID";

	ArtObjectAssociationRecord(ArtDataManagerService manager) {
		super(manager);
	}

	ArtObjectAssociationRecord(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		// order of query is as defined above
		super(manager, 	  	  TypeUtils.getLong(rs, 1));
		this.associationID	= TypeUtils.getLong(rs, 2);
		this.parentObjectID = TypeUtils.getLong(rs, 3);
		this.childObjectID	= TypeUtils.getLong(rs, 4);
		divisibility	= ARTOBJECTDIVISIBILITY.fromLabel(rs.getString(5));
	}

	public ArtObjectAssociationRecord factory(ResultSet rs) throws SQLException {
		ArtObjectAssociationRecord aoa = new ArtObjectAssociationRecord(getManager(), rs);
		return aoa;
	}

	public String getKeyValue() {
		return getAssociationID().toString();
	}

	private Long associationID = null;
	Long getAssociationID() {
		return associationID;
	}

	private Long parentObjectID = null;
	Long getParentObjectID() {
		return parentObjectID;
	}
	private ArtObject getParentArtObject() {
		return getManager().fetchByObjectID(getParentObjectID());
	}

	private Long childObjectID = null;
	Long getChildObjectID() {
		return childObjectID;
	}
	private ArtObject getChildArtObject() {
		return getManager().fetchByObjectID(getChildObjectID());
	}

	private ARTOBJECTDIVISIBILITY divisibility = null;
	private ARTOBJECTDIVISIBILITY getDivisibility() {
		return divisibility;
	}

	ArtObjectAssociation getAssociationContext(ArtObject referenceArtObject) {
		// if the child or parent art object is null, there's no point in proceeding further
		if (this.getChildArtObject() == null || this.getParentArtObject() == null)
			return null;
		if ( this.getParentObjectID().equals(referenceArtObject.getObjectID())) {
			return new ArtObjectAssociation(
				this.getDivisibility(), 
				ARTOBJECTRELATIONSHIPROLE.PARENT,
				this.getChildArtObject(), referenceArtObject
			);
		}
		else if ( this.getChildObjectID().equals(referenceArtObject.getObjectID())) {
			return new ArtObjectAssociation(
				this.getDivisibility(), 
				ARTOBJECTRELATIONSHIPROLE.CHILD,
				this.getParentArtObject(), referenceArtObject
			);
		}
		else if (    referenceArtObject.getParentAssociation() != null 
				  && referenceArtObject.getParentAssociation().getAssociatedArtObject().equals(this.getParentArtObject()))	{
			ARTOBJECTDIVISIBILITY refParentDivisibility = referenceArtObject.getParentAssociation().getDivisibility();
			ARTOBJECTDIVISIBILITY assParentDivisibility = this.getDivisibility();
			// default is inseparable divisibility, but if either of the siblings has a separable divisibility with the parent 
			// then the two sibling objects must also be separable
			ARTOBJECTDIVISIBILITY sibDivisibility = ARTOBJECTDIVISIBILITY.INSEPARABLE;
			if (refParentDivisibility.equals(ARTOBJECTDIVISIBILITY.SEPARABLE) || assParentDivisibility.equals(ARTOBJECTDIVISIBILITY.SEPARABLE))
				sibDivisibility = ARTOBJECTDIVISIBILITY.SEPARABLE;
			return new ArtObjectAssociation(
				sibDivisibility, 
				ARTOBJECTRELATIONSHIPROLE.SIBLING,
				this.getChildArtObject(), referenceArtObject
			);
		}
		return null;
	}


}
