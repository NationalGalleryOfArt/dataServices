package gov.nga.entities.art.datamanager.data;

import java.util.List;

import gov.nga.common.entities.art.ArtEntity;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.search.ResultsPaginator;
import gov.nga.common.utils.CollectionUtils;

public class QueryResultArtDataLocal<T extends ArtEntity> extends QueryResultLocal<T> implements QueryResultArtData<T>
{
	
	protected QueryResultArtDataLocal(final List<T> rslts)
	{
		super(rslts);
	}
	
	protected QueryResultArtDataLocal(final List<T> rslts, final ResultsPaginator rp)
	{
		super(rslts, rp);
	}
}
