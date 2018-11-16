/*
    NGA ART DATA API: ArtObjectPredicates represents relationships that can be associated
    with an art object - it is incomplete (as is this entire API which will evolve over time).    

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


import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import gov.nga.utils.ConfigService;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)  // default is singleton, but best to be explicit
public class ArtObjectPredicates extends Predicates {
	
	//private static final Logger log = LoggerFactory.getLogger(ImagePredicates.class);

	// TODO - see Predicates class where this is also defined - do we really need this
	// in both places? probably not
	@Autowired
	ConfigService config;	// access to application.properties file

	private static final String hasParentLabel 	= "nga.api.art.lod.predicates.labels.hasParent";
	private static final String hasChildLabel 	= "nga.api.art.lod.predicates.labels.hasChild";
	private static final String hasSiblingLabel = "nga.api.art.lod.predicates.labels.hasSibling";
	private static final String producedByLabel	= "nga.api.art.lod.predicates.labels.producedBy";
	
	public enum ARTOBJECTPREDICATES implements EnumLabeledInterface {

		HASPARENT,
		HASCHILD,
		HASSIBLING,
		PRODUCEDBY;

		private void setLabels(String[] labels) {
			EnumLabeledInterface.setLabels(testMode, ARTOBJECTPREDICATES.class.getName(), toString(), labels);
		}
		
		public String[] getLabels() {
			return EnumLabeledInterface.getLabels(ARTOBJECTPREDICATES.class.getName(), toString());
		}

		
	};

	@PostConstruct
	private void init() {
		ARTOBJECTPREDICATES.HASPARENT.setLabels(config.getStrings(hasParentLabel,","));
		ARTOBJECTPREDICATES.HASCHILD.setLabels(config.getStrings(hasChildLabel,","));
		ARTOBJECTPREDICATES.HASSIBLING.setLabels(config.getStrings(hasSiblingLabel,","));
		ARTOBJECTPREDICATES.PRODUCEDBY.setLabels(config.getStrings(producedByLabel,","));
	}

	
	// initialize the enum when this service starts up
	public ArtObjectPredicates() {
	}

}