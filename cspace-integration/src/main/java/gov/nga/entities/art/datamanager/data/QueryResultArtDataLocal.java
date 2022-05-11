package gov.nga.entities.art.datamanager.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gov.nga.common.entities.art.ArtEntity;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.search.Facet;
import gov.nga.common.search.ResultsPaginator;

public class QueryResultArtDataLocal<T extends ArtEntity> extends QueryResultLocal<T> implements QueryResultArtData<T>
{
	final List<Facet> facets;
	
	protected QueryResultArtDataLocal(final List<T> rslts)
	{
		super(rslts);
		facets = Collections.emptyList();
	}
	
	protected QueryResultArtDataLocal(final List<T> rslts, final ResultsPaginator rp)
	{
		super(rslts, rp);
		facets = Collections.emptyList();
	}
	
	protected QueryResultArtDataLocal(final List<T> rslts, final ResultsPaginator rp, final List<Facet> facets)
	{
		super(rslts, rp);
		this.facets = (facets == null) ? Collections.emptyList() : facets;
	}
	
	@Override
	public Collection<Facet> getFacetResults()
	{
		return facets;
	}
}
