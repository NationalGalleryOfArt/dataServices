package gov.nga.rpc.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.EnumUtils;
import gov.nga.common.entities.art.ArtDataSuggestion;
import gov.nga.common.entities.art.SuggestType;
import gov.nga.common.rpc.ArtDataSuggestionResult;
import gov.nga.common.rpc.SuggestArtDataQuery;
import gov.nga.entities.art.ArtDataManager;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats.TMSOperation;
import io.grpc.stub.StreamObserver;
import gov.nga.common.proto.messages.ArtDataSuggestionMessageFactory;

public class SuggestionHelper extends TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(SuggestionHelper.class);

	protected SuggestionHelper(ArtDataManager mgr, final GrpcTMSStats statsMonitor) 
	{
		super(mgr, statsMonitor);
	}
	
	public void getSuggestions(final SuggestArtDataQuery request, 
			final StreamObserver<ArtDataSuggestionResult> responseObserver)
	{
		final SuggestType type = EnumUtils.getEnumValue(SuggestType.class, request.getType());
		final List<ArtDataSuggestion> results = CollectionUtils.newArrayList();

		try
		{
			if (type != null && StringUtils.isNotBlank(request.getTerm()))
			{
				final String term1 = request.getTerm().trim();
				switch(type)
				{
					case ARTIST_TITLE: 
					case ARTIST_TITLE_ID:
						results.addAll(getQueryManager().suggestArtists(term1).getResults());
						break;
					case ARTOBJECT_TITLE: 
					case ARTOBJECT_TITLE_ID:
						results.addAll(getQueryManager().suggestArtObjects(term1).getResults());
						break;
					case ARTOBJECT_WITH_ARTIST:
						final String termTitle = request.getSecondterm();
						if (StringUtils.isNotBlank(termTitle))
						{
							results.addAll(getQueryManager().suggestArtObjectFromArtist(term1, termTitle.trim())
									.getResults());
						}
						break;
					case PROVENANCE_TITLE_ID: 
					case PROVENANCE_TITLE:
						results.addAll(getQueryManager().suggestOwners(term1).getResults());
						break;
					case EXHIBITION_TITLE:
						results.addAll(getQueryManager().suggestExhibitions(term1).getResults());
						break;
				}
			}
			
			for (ArtDataSuggestion rslt: results)
			{
				gov.nga.common.entities.art.proto.ArtDataSuggestion cand = 
						ArtDataSuggestionMessageFactory.createMsgFromPOJO(rslt);
				if (cand != null)
				{
					responseObserver.onNext(ArtDataSuggestionResult.newBuilder().setSuggestion(cand).build());
				}
			}
			responseObserver.onCompleted();
			reportCall(TMSOperation.SUGGESTION, results.size());
		}
		catch (final Exception err)
		{
			LOG.error("getSuggestions(): Exception Caught", err);
			responseObserver.onError(err);
		}
	}
}
