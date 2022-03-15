package gov.nga.entities.art.datamanager.data;

import java.util.List;

import gov.nga.common.imaging.NGAImage;

public class QueryResultNGAImage<T extends NGAImage> extends QueryResultLocal<T>
{
	protected QueryResultNGAImage(final List<T> rslts)
	{
		super(rslts);
	}
}
