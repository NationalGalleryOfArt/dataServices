/*
    NGA ART DATA API: MediaPredicates represent the types of relationships that are associated with
    media items and other entities represented by the art data APIs

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
public class MediaPredicates extends Predicates {
	
	//private static final Logger log = LoggerFactory.getLogger(ImagePredicates.class);

	@Autowired
	ConfigService config;	// access to application.properties file

	private static final String hasPrimaryDepictionLabel 	= "nga.api.art.lod.predicates.labels.hasPrimaryDepiction";
	private static final String hasDepictionLabel 			= "nga.api.art.lod.predicates.labels.hasDepiction";
	private static final String primarilyDepictsLabel 		= "nga.api.art.lod.predicates.labels.primarilyDepicts";
	private static final String depictsLabel 				= "nga.api.art.lod.predicates.labels.depicts";
	private static final String refersToLabel 				= "nga.api.art.lod.predicates.labels.refersTo";
	private static final String isReferredToByLabel 		= "nga.api.art.lod.predicates.labels.isReferredToBy";
	
	public enum MEDIAPREDICATES implements EnumLabeledInterface {

		HASPRIMARYDEPICTION, 
		HASDEPICTION,
		PRIMARILYDEPICTS,
		DEPICTS,
		REFERSTO,
		ISREFERREDTOBY;
		
		private void setLabels(String[] labels) {
			EnumLabeledInterface.setLabels(testMode, MEDIAPREDICATES.class.getName(), toString(), labels);
		}
		
		public String[] getLabels() {
			return EnumLabeledInterface.getLabels(MEDIAPREDICATES.class.getName(), toString());
		}
		
	};

	@PostConstruct
	private void init() {
		MEDIAPREDICATES.HASPRIMARYDEPICTION.setLabels(config.getStrings(hasPrimaryDepictionLabel,","));
		MEDIAPREDICATES.HASDEPICTION.setLabels(config.getStrings(hasDepictionLabel,","));
		MEDIAPREDICATES.PRIMARILYDEPICTS.setLabels(config.getStrings(primarilyDepictsLabel,","));
		MEDIAPREDICATES.DEPICTS.setLabels(config.getStrings(depictsLabel,","));
		MEDIAPREDICATES.REFERSTO.setLabels(config.getStrings(refersToLabel,","));
		MEDIAPREDICATES.ISREFERREDTOBY.setLabels(config.getStrings(isReferredToByLabel,","));
	}

	
	// initialize the enum when this service starts up
	public MediaPredicates() {
	}

}