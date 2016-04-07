package gov.nga.entities.art;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.search.FreeTextSearchable;


public class ArtEntityFreeTextSearch<T extends ArtEntity> implements FreeTextSearchable<T>{
	
	private static final Logger log = LoggerFactory.getLogger(ArtEntityFreeTextSearch.class);
	
	
	public List<T> freeTextSearch(List<Object> fields, String searchTerm, List<T> baseList) {
		log.error("*********** FREE TEXT SEARCH NOT IMPLEMENTED ***************");
		return baseList;
	}
		

}