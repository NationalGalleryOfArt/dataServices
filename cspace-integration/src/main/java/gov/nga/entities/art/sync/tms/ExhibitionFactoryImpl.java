package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.Constituent;
import gov.nga.utils.db.DataSourceService;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionLoan;
import gov.nga.common.entities.art.ExhibitionVenue;
import gov.nga.common.utils.CollectionUtils;


public class ExhibitionFactoryImpl implements TMSExhibitionFactory
{
    static final Logger LOG = LoggerFactory.getLogger(ExhibitionFactoryImpl.class);
    
    private final Map<Long, Exhibition> exhibitionsMap = CollectionUtils.newHashMap();
    private final Map<Long, ExhibitionVenue> venueMap = CollectionUtils.newHashMap();

    @Override
    public Map<Long, Exhibition> getExhibitions(final Map<Long, ArtObject> artObjectMap, final Map<Long, Constituent> constituentMap, final DataSourceService ps) 
    {
        buildObjects(artObjectMap, constituentMap, ps);
        return exhibitionsMap;
    }
    
    
    synchronized private void buildObjects(final Map<Long, ArtObject> artObjectMap, final Map<Long, Constituent> constituentMap, final DataSourceService ps)
    {
        final TMSQuerier querier = new TMSQuerier(ps);
        try
        {
            buildExhibitions(querier);
            LOG.info("Loaded iniital Exhibitions: " + exhibitionsMap.size());
            buildVenues(constituentMap, querier);
            final Map<Long, ExhibitionLoan> loans = buildLoans(querier, constituentMap);
            LOG.info("Loan Map: " + loans.size());
            buildArtObjects(querier, artObjectMap, loans);
            buildConstituents(querier, constituentMap);
        }
        catch (final SQLException err)
        {
            LOG.error("SQlException occurred while building Exhibitions data", err);
        }
    }
    
    private void buildExhibitions(final TMSQuerier querier) throws SQLException
    {
        querier.getQueryResults(TMSQuery.EXHIBITION.getConstantValue(), new ExhibitionFactory());
        //LOG.info("Initial Exhibitions map built: " + exhibitionsMap);
    }

    private void buildVenues(final Map<Long, Constituent> constituentMap, final TMSQuerier querier) throws SQLException
    {
        querier.getQueryResults(TMSQuery.VENUE.getConstantValue(), new VenueFactory(constituentMap, exhibitionsMap));
    }
    
    private Map<Long, ExhibitionLoan> buildLoans(final TMSQuerier querier, final Map<Long, Constituent> constituentMap) throws SQLException
    {
        final LoanFactory factory = new LoanFactory(constituentMap);
        querier.getQueryResults(TMSQuery.EXHIBITION_LOAN.getConstantValue(), factory);
        return factory.getLoanMap();
    }
    
    private void buildArtObjects(final TMSQuerier querier, final Map<Long, ArtObject> artObjects, final Map<Long, ExhibitionLoan> loans) throws SQLException
    {
        querier.getQueryResults(TMSQuery.EXHIBITION_ARTOBJECT.getConstantValue(), new ArtObjectFactory(exhibitionsMap, artObjects, loans));
    }
    
    private void buildConstituents(final TMSQuerier querier, final Map<Long, Constituent> constituents) throws SQLException
    {
        querier.getQueryResults(TMSQuery.EXHIBITION_CONSTITUENT.getConstantValue(), new ConstituentFactory(exhibitionsMap, constituents));
    }
    
    class ExhibitionFactory implements TMSEntityFactory
    {

        @Override
        public void processResult(ResultSet rs) throws SQLException 
        {
            try
            {
                TMSExhibition exh = new TMSExhibition(rs);
                exhibitionsMap.put(exh.getID(), exh);
            }
            catch (final Exception err)
            {
                LOG.error("Caught an exception trying to create an exhibition", err);
            }
        }
    }
    
    class VenueFactory implements TMSEntityFactory
    {
        final Map<Long, Constituent> constituentsMap;
        final Map<Long, Exhibition> exhibitionMap;
        
        VenueFactory(final Map<Long, Constituent> constituentMap, final Map<Long, Exhibition> exhibitionMap)
        {
            this.constituentsMap = constituentMap;
            this.exhibitionMap = exhibitionMap;
        }

        @SuppressWarnings("unused")
        @Override
        public void processResult(ResultSet rs) throws SQLException 
        {
            try
            {
                new TMSVenue(rs, constituentsMap, exhibitionMap);
            }
            catch (final Exception err)
            {
                LOG.error("Caught an exception trying to create an exhibition venue", err);
            }
        }
        
    }
    
    class LoanFactory implements TMSEntityFactory
    {
        private final Map<Long, ExhibitionLoan> loanMap = CollectionUtils.newHashMap();
        private final Map<Long, Constituent> constituentMap;
        
        LoanFactory (final Map<Long, Constituent> cMap)
        {
            constituentMap = cMap;
        }
        
        
        Map<Long, ExhibitionLoan> getLoanMap()
        {
            return loanMap;
        }

        @Override
        public void processResult(ResultSet rs) throws SQLException 
        {
            try
            {
                TMSLoan loan = new TMSLoan(rs, constituentMap);
                loanMap.put(loan.getID(), loan);
            }
            catch (final Exception err)
            {
                LOG.error("Caught an exception trying to create an exhibition loan", err);
            }
        }
    }
    
    class ArtObjectFactory implements TMSEntityFactory
    {
        private final Map<Long, Exhibition> exhibitions;
        private final Map<Long, ArtObject> artobjects;
        private final Map<Long, ExhibitionLoan> loans;
        
        ArtObjectFactory (final Map<Long, Exhibition> exhibitions, Map<Long, ArtObject> artobjects, Map<Long, ExhibitionLoan> loans)
        {
            this.exhibitions = exhibitions;
            this.artobjects = artobjects;
            this.loans = loans;
        }

        @SuppressWarnings("unused")
        @Override
        public void processResult(ResultSet rs) throws SQLException 
        {
            try
            {
                new TMSExhibitionArtObject(rs, exhibitions, artobjects, loans);
                
            }
            catch (final Exception err)
            {
                LOG.error("Caught an exception trying to create an exhibition ArtObject", err);
            }
        }
    }
    
    class ConstituentFactory implements TMSEntityFactory
    {
        private final Map<Long, Exhibition> exhibitions;
        private final Map<Long, Constituent> constituents;
        
        ConstituentFactory (final Map<Long, Exhibition> exhibitions, Map<Long, Constituent> constituents)
        {
            this.exhibitions = exhibitions;
            this.constituents = constituents;
        }

        @Override
        public void processResult(ResultSet rs) throws SQLException 
        {
            try
            {
                TMSExhibitionConstituent cand = new TMSExhibitionConstituent(rs, exhibitions, constituents);
                ((TMSExhibition)cand.getExhibition()).addConstituent(cand);
            }
            catch (final Exception err)
            {
                LOG.warn("Caught an exception trying to create an exhibition Constituent", err);
            }
        }
    }
}
