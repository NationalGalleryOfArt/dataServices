/*
    NGA ART DATA API: SourceProvider is an interface that ensures implementations can provide the source
    from which their data originated.  The LinkedArt data model makes no allowance for this and I suspect 
    the idea would be for the source to either be hidden entirely from the URLs of LinkedArt entities although
    to avoid ID conflicts for entities with multiple sources might require the source to become part of the
    id or for alternative IDs to be used.  We'll have to discuss this as an issue within LinkedArt once those
    discussions get underway in earnest again. 
  
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

public interface SourceProvider {
	public boolean providesSource(String[] sources);
	public String[] getProvidedSources();
}
