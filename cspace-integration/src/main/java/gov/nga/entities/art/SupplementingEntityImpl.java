/*
    NGA Art Data API: Base Art Entity Implementation from which other entity implementations are derived  

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
package gov.nga.entities.art;

public abstract class SupplementingEntityImpl extends ArtEntityImpl implements SupplementingEntity
{
	protected SupplementingEntityImpl(ArtDataManagerService manager, Long fingerprint) {
		super(manager, fingerprint);
	}

	private Class<?> tmsEntityClass = null;
	
	public Class<?> getTMSEntityClass() {
		return tmsEntityClass;
	}

	public void setArtObjectID(Long tmsObjectID) {
		if (tmsObjectID != null) {
			setEntityID(tmsObjectID);
			this.tmsEntityClass = ArtObject.class;
		}
	}

	public Long getArtObjectID() {
		if ( tmsEntityClass == ArtObject.class)
			return getEntityID();
		return null;
	}

	// these are getting called automatically when the bean is
	// copied which is creating problems down the line since
	// setConstituentID is the last in the chain to be called
	// and therefore b
	public void setConstituentID(Long constituentID) {
		if (constituentID != null) {
			setEntityID(constituentID);
			this.tmsEntityClass = Constituent.class;
		}
	}

	@Override
	public String getEntityKey() {
		if ( this.tmsEntityClass == ArtObject.class)
			return ArtObject.getEntityKeyStatic();
		else if ( this.tmsEntityClass == Constituent.class)
			return Constituent.getEntityKeyStatic();
		else return null;
	}
	
	public Long getConstituentID() {
		if ( tmsEntityClass == Constituent.class)
			return getEntityID();
		return null;
	}

	public ArtObject getArtObject() {
		if (getArtObjectID() == null)
			return null;
		return getManager().fetchByObjectID(getArtObjectID());
	}
	
	public Constituent getConstituent() {
		if (getConstituentID() == null)
			return null;
		return getManager().fetchByConstituentID(getConstituentID());
	}
	
}
