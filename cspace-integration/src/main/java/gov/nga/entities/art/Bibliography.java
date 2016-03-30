package gov.nga.entities.art;

import gov.nga.utils.TypeUtils;

import java.util.Comparator;

public interface Bibliography  {
	
	public static Comparator<Bibliography> sortByYearPublishedAsc = new Comparator<Bibliography>() {
		public int compare(Bibliography a, Bibliography b) {
			return TypeUtils.compare(a.getYearPublished(), b.getYearPublished());
		}
	};

	public Long getYearPublished();
	public String getCitation();

}
