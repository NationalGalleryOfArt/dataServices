package gov.nga.rpc.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.proto.messages.ExhibitionMessageFactory;
import gov.nga.common.rpc.FetchByIDsQuery;
import gov.nga.common.rpc.message.QueryMessage;
import gov.nga.common.search.SortHelper;
import gov.nga.common.rpc.ExhibitionObjectResult;
import gov.nga.common.rpc.ExhibitionQueryResult;
import gov.nga.entities.art.ArtDataManager;
import io.grpc.stub.StreamObserver;

public class ExhibitionHelper extends TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(ExhibitionHelper.class);

	protected ExhibitionHelper(ArtDataManager mgr) {
		super(mgr);
	}
	
	protected void fetchExhibitions(final FetchByIDsQuery request,
			final StreamObserver<ExhibitionObjectResult> responseObserver)
	{
		LOG.debug("fetchExhibitions() called..."); 
		try
		{
			final ExhibitionObjectResult.Builder builder = ExhibitionObjectResult.newBuilder();
			final List<Exhibition> rslts = getQueryManager().fetchByExhibitionIDs(request.getIdsList()).getResults();
			for (Exhibition rslt: rslts)
			{
				builder.setExhibition(ExhibitionMessageFactory.createMessage(rslt));
				responseObserver.onNext(builder.build());
			}
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("fetchExhibitions(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	protected void searchExhibitions(final QueryMessage request,
			final StreamObserver<ExhibitionQueryResult> responseObserver)
	{

		LOG.debug("searchExhibitions() called..."); 
		try
		{
			final QueryResultArtData<Exhibition> rslts = processRequest(request);
			final ExhibitionQueryResult.Builder builder = ExhibitionQueryResult.newBuilder();
			if (rslts == null)
			{
				builder.setTotalResults(0);
			}
			else
			{
				builder.setTotalResults(rslts.getResultCount());
				for (Exhibition rt: rslts.getResults())
				{
					builder.addIds(rt.getID());
				}
			}
			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		}
		catch (final Exception err)
		{
			LOG.error("searchExhibitions(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
	
	private QueryResultArtData<Exhibition> processRequest(final QueryMessage request)
	{

		gov.nga.common.rpc.api.QueryMessage<Exhibition> args = 
				getQueryMessagePOJO(Exhibition.class, Exhibition.SORT.class, Exhibition.SEARCH.class, request);
		
		if (args.getObjectIDs().size() > 0)
		{
			LOG.debug("Making fetch call with no sort: " + args.getObjectIDs());
			return getQueryManager().fetchByExhibitionIDs(args.getObjectIDs());			
		}
		
		LOG.debug("this is not a fetch call");
		if (args.getSrchHlpr() != null)
		{
			if (args.getOrder() == null)
			{
				LOG.debug("Making search call with no sort: ");
				return getQueryManager().searchExhibitions(args.getSrchHlpr(), args.getPgn(), null);
			}
			else
			{
				LOG.debug(String.format("Making search call with sort: %s", args.getOrder().getSortOrder()));
				return getQueryManager().searchExhibitions(args.getSrchHlpr(), args.getPgn(), new SortHelper<Exhibition>(args.getOrder()));
			}
			
		}
		return null;
	}

}
