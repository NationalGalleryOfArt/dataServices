/*
    NGA ART DATA API: Predicates provides some common routines used by ArtObjectPredicates 
    and MediaPredicates - although some of the fields seem duplicative so this hierarchy might need
    a closer look
  
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
public class Predicates {
	
	//private static final Logger log = LoggerFactory.getLogger(ImagePredicates.class);

	@Autowired
	ConfigService config;	// access to application.properties file

	@Autowired
	CSpaceTestModeService ts;
	
	protected static boolean testMode = false;

	@PostConstruct
	private void init() {
		testMode = ts.isTestModeOtherHalfObjects();
	}
	
	public Predicates() {
	}
	
}