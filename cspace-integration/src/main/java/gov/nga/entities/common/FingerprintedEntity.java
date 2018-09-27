/*
    NGA Art Entity Commons: Fingerprinted entity is an abstract class
    that provides a fingerprint member.  The intention is to provide in
    sub-classes the ability to identify when data records have changed.
    Fingerprint might not be the best word choice since the intention is not
    to provide a GUID, but to provide a member that is persisted with the data
    for the object and can later be re-computed to determine whether data has
    changed.  A more useful implementation would be to provide a mechanism for
    simply comparing sets of attributes since not all attributes are used by
    downstream data consumers.  For example, image generation.  With metadata 
    from art entities injected into images of those entities, the image processes
    would only care if data about the entity had changed that's actually being
    used in the injected metadata.  I think having a last modification timestamp
    along with the capability to define multiple mechanisms for comparing / detecting
    change would, in general, be more useful than a (usually database) computed 
    fingerprint of all fields.  

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
package gov.nga.entities.common;

public abstract class FingerprintedEntity implements FingerprintedInterface { 

	private Long fingerprint = null;
	
	public FingerprintedEntity() {
	}
	
//	public FingerprintedEntity(ResultSet rs) throws SQLException {
//	}
	
	public FingerprintedEntity(Long fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	public boolean sameFingerprint(FingerprintedEntity d) {
		return this.getFingerprint().equals(d.getFingerprint());
	}
	
	public Long getFingerprint() {
		return fingerprint;
	}

}