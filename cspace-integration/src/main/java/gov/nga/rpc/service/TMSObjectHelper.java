package gov.nga.rpc.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.rpc.message.QueryMessage;
import gov.nga.common.search.FacetHelper;
import gov.nga.common.search.Faceted;
import gov.nga.common.search.ResultsPaginator;
import gov.nga.common.search.SearchHelper.SEARCHOP;
import gov.nga.common.search.Searchable;
import gov.nga.common.search.Sortable;
import gov.nga.common.utils.EnumUtils;
import gov.nga.entities.art.ArtDataManager;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats.TMSOperation;
import gov.nga.utils.CollectionUtils;

public class TMSObjectHelper 
{
	private static final Logger LOG = LoggerFactory.getLogger(TMSObjectHelper.class);
	
	final ArtDataManager manager;
	final GrpcTMSStats statsMonitor;
	
	protected TMSObjectHelper(final ArtDataManager mgr, final GrpcTMSStats statsMonitor)
	{
		manager = mgr;
		this.statsMonitor = statsMonitor;
	}
	
	protected void reportCall(final TMSOperation type, final int numOfObjects) 
	{
		statsMonitor.reportTransaction(type, numOfObjects);
	}
	
	protected ArtDataManager getManager()
	{
		return manager;
	}
	
	protected ArtDataQuerier getQueryManager()
	{
		return manager.getArtDataQuerier();
	}
	
	protected static <T extends Faceted & Searchable & Sortable, E extends Enum<E>, S extends Enum<S>> gov.nga.common.rpc.api.QueryMessage<T> getQueryMessagePOJO
				(final Class<T> clss, final Class<E> sortClass, final Class<S> searchClass,
						final gov.nga.common.rpc.message.QueryMessage qMsg)
	{
		gov.nga.common.rpc.api.QueryMessage<T> pojo = new gov.nga.common.rpc.api.QueryMessage<T>();
		if (qMsg != null)
		{
			if (qMsg.getObjectIDCount() > 0)
			{
				pojo.setObjectIDs(qMsg.getObjectIDList());
			}
			if (qMsg.hasPgn())
			{
				pojo.setPgn(new ResultsPaginator(qMsg.getPgn().getPagesize(), qMsg.getPgn().getPage()));
			}
			if (qMsg.getSortOrderCount() > 0)
			{
				List<Enum<?>> orders = CollectionUtils.newArrayList();
				for (gov.nga.common.rpc.message.QueryMessage.SortOrder mOrder: qMsg.getSortOrderList())
				{ 
					/*
					try 
					{
						LOG.info(String.format("Test: %s", Class.forName(Constituent.SORT.class.getCanonicalName(), true, Constituent.class.getClassLoader())));
						Enum<?> cand = EnumUtils.getEnumValue(
								(Class<Enum>) Class.forName(mOrder.getEnumClass(), true, Thread.currentThread().getContextClassLoader()), 
								mOrder.getEnumValue());
						if (cand != null)
						{
							orders.add(cand);
						}
					} 
					catch (final Exception e) 
					{
						LOG.warn(String.format("Problems estantiating sort class: %s", mOrder.getEnumClass()), e);
					}*/
					Enum<E> cand = EnumUtils.getEnumValue(sortClass, mOrder.getEnumValue());
					if (cand != null)
					{
						orders.add(cand);
					}
				}
				pojo.setOrder(new gov.nga.common.search.SortOrder(orders));
			}
			if (qMsg.hasSrchHlpr())
			{
				gov.nga.common.search.SearchHelper<T> srchH = new gov.nga.common.search.SearchHelper<T>();
				for (gov.nga.common.search.SearchFilter srchFltr: buildSerchFilters(searchClass, qMsg.getSrchHlpr().getFiltersList()))
				{
					srchH.addFilter(srchFltr);
				}
				pojo.setSrchHlpr(srchH);
			}
			
			if (qMsg.getFacetsCount() > 0)
			{
				final List<String> facets = CollectionUtils.newArrayList();
				for (gov.nga.common.rpc.message.QueryMessage.FacetHelper facet: qMsg.getFacetsList())
				{
					facets.add(facet.getName());
				}
				pojo.setFacetHlpr(new FacetHelper(facets.toArray(new String[] {})));
			}
		}
		return pojo;
	}
	
	protected static <E extends Enum<E>> List<gov.nga.common.search.SearchFilter> buildSerchFilters 
				(final Class<E> searchClass, final List<QueryMessage.SearchHelper.SearchFilter> source)
	{
		final List<gov.nga.common.search.SearchFilter> filters = CollectionUtils.newArrayList();
		if (source != null)
		{
			for (QueryMessage.SearchHelper.SearchFilter srcFltr: source)
			{
				SEARCHOP op = EnumUtils.getEnumValue(SEARCHOP.class, srcFltr.getOp().name());
				Enum<E> field = EnumUtils.getEnumValue(searchClass, srcFltr.getFieldValue());
				Boolean normalize = srcFltr.getNormalize();
				if (srcFltr.getStringsCount() > 0)
				{
					filters.add(new gov.nga.common.search.SearchFilter(op, field, srcFltr.getStringsList(), normalize));
				}
				else
				{
					LOG.warn("Search filter had no search strings. Skipping....");
				}
			}
		}
		return filters;
	}
	
	protected static String formatToSeconds(final long miliseconds)
	{
		String rtrnTxt = null;
		try
		{
			rtrnTxt = String.format("%.4f seconds", ((double)miliseconds / 1000));
		}
		catch (final Exception err)
		{
			LOG.warn(String.format("Exception in formatToSeconds(%d)", miliseconds), err);
			rtrnTxt = "" + miliseconds;
		}
		return rtrnTxt;
	}
}
