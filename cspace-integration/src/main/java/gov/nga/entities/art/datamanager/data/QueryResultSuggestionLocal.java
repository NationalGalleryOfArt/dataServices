package gov.nga.entities.art.datamanager.data;

import java.util.List;

import gov.nga.common.entities.art.ArtDataSuggestion;
import gov.nga.common.entities.art.QueryResultSuggestion;

public class QueryResultSuggestionLocal<T extends ArtDataSuggestion> extends QueryResultLocal<T> implements QueryResultSuggestion<T>
{
	protected QueryResultSuggestionLocal(final List<T> rslts)
	{
		super(rslts);
	}
}
