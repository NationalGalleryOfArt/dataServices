/*
    NGA ART DATA API: DCLPADataSource provides a data source for an additional DAM database that
    needs to be searched for images (in addition to the primary DAM database).

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

import gov.nga.utils.ConfigService;
import gov.nga.utils.db.DataSourceService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author David Beaudet
 *
 */
@Configuration(value="nga.jdbc.dclpa")
public class DCLPADataSource extends DataSourceService {

	private static final Logger log = LoggerFactory.getLogger(DCLPADataSource.class);
	
	private static final String dbURL 	= "nga.jdbc.dclpa.url";
	private static final String dbUser 	= "nga.jdbc.dclpa.username";
	private static final String dbPass	= "nga.jdbc.dclpa.password";
	private static final String dbValid = "nga.jdbc.dclpa.validationQuery";
	
	@Autowired
	ConfigService config;	// access to application.properties file

	@PostConstruct
	private void postConstruct() {
		init(
				config.getString(dbURL),
				config.getString(dbUser),
				config.getString(dbPass),
				config.getString(dbValid)
		);
	}
	
	@PreDestroy
	private void preDestroy() {
		log.debug("Nothing to destroy here since Spring's pool implementation is responsible for closing any open connections.");
	}

}