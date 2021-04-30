package gov.nga.entities.art.datamanager.data;

import java.util.List;

import gov.nga.common.entities.art.QueryResult;
import gov.nga.common.search.ResultsPaginator;
import gov.nga.common.utils.CollectionUtils;

public abstract class QueryResultLocal<T> implements  QueryResult<T>
{
	private final List<T> results;
	private final Integer count;
	
	protected QueryResultLocal()
	{
		this(null, 0);
	}
	
	protected QueryResultLocal(final List<T> rslts)
	{
		if (rslts == null)
		{
			results = CollectionUtils.newArrayList();
			count = 0;
		}
		else
		{
			results = rslts;
			count = rslts.size();
		}
	}
	
	protected QueryResultLocal(final List<T> rslts, final int cnt)
	{
		if (rslts == null)
		{
			results = CollectionUtils.newArrayList();
		}
		else
		{
			results = rslts;
		}
		count = cnt;
	}
	
	protected QueryResultLocal(final List<T> rslts, final ResultsPaginator rp)
	{
		if (rslts == null)
		{
			results = CollectionUtils.newArrayList();
		}
		else
		{
			results = rslts;
		}
		count = (rp == null) ? results.size() : rp.getTotalResults();
	}

	@Override
	public List<T> getResults() 
	{
		return results;
	}

	@Override
	public int getResultCount() 
	{
		return count;
	}
}
