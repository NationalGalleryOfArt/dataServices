/*
    NGA ART DATA API: CSpaceTestModeService defines an interface for providing testing modes that
    were required in order to thoroughly test ConservationSpace's multi-tenancy features.  This class
    can probably be retired now that multi-tenancy testing is over although I suppose a similar need
    could arise in the future so it might just be safer to ignore this for now and leave it in place.

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

public interface CSpaceTestModeService {

	public boolean isTestModeHalfObjects();
	
	public boolean isTestModeOtherHalfObjects();
	
	public boolean unloadBeforeLoading();

}
