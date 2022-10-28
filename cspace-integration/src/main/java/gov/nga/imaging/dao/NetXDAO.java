package gov.nga.imaging.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.imaging.NGAImage;
import gov.nga.jdbc.JdbcTemplate;
import gov.nga.utils.ConfigService;
import gov.nga.utils.db.DataSourceService;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.Constant;


public abstract class NetXDAO implements NetXImageDAO 
{

    private static final Logger LOGGER = LoggerFactory.getLogger(NetXDAO.class);

    public DataSourceService poolService;
    
    private static enum QUERY_PARTS implements Constant<String> {
        BASE_QUERY("select * from data.published_images as pi "),
        OBJECT_CLAUSE("depictstmsobjectid > -1"),
        NONOBJECT_CLAUSE("depictstmsobjectid IS null"),
        OBJECTID_CLAUSE("depictstmsobjectid = ");
        
        private final String value;
        
        QUERY_PARTS(final String v) {
            value = v;
        }
        
        @Override
        public String getConstantValue() {
            return value;
        }
        
    }
    

    
    @Override
    public List<NGAImage> getImagesForObject(final long objectID) {
        final List<NGAImage> images = CollectionUtils.newArrayList();
        try {
            final String query = buildQuery("WHERE", String.format("%s %d", QUERY_PARTS.OBJECTID_CLAUSE.getConstantValue(), objectID), "order by sequence");
            LOGGER.debug("getImagesForObject() query: " + query);
            images.addAll(new JdbcTemplate(getImageryDS()).query(query, null, createRowMapper()));
        } catch (final SQLException err) {
            LOGGER.error("getImagesForObject has thrown a SQL Exception", err);
        }
        LOGGER.debug(String.format("getImagesForObject(%d): %s", objectID, images));
        return images;
    }
    
    @Override
    public NGAImage getImageByID(final String id)
    {
        NGAImage img = null;
        if (StringUtils.isNotBlank(id))
        {
            final Map<String, Object> params = CollectionUtils.newHashMap();
            params.put("uuid", id);
            final List<NGAImage> images = getImageByQuery(params);
            if (images.size() == 1)
            {
                img = images.get(0);
            }
        }
        return img;
    }
    
    
    public List<NGAImage> getAllImages() {
        final List<NGAImage> images = CollectionUtils.newArrayList();
        try {
            images.addAll(new JdbcTemplate(getImageryDS()).query(buildQuery(), null, createRowMapper()));
        } catch (final SQLException err) {
            LOGGER.error("getAllImages() has thrown a SQL Exception", err);
        }
        return images;
    }
    
    private static final String BASE_IMAGE_QUERY = "select * from data.published_images where ";
    @Override
    public List<NGAImage> getImageByQuery(final Map<String, Object> params) {
        final List<NGAImage> images = CollectionUtils.newArrayList();
        if (params != null && params.size() > 0) {
            final StringBuilder query = new StringBuilder();
            final String StringTemplate = "%s = '%s'";
            final String IntegerTemplate = "%s = %s";
            query.append(BASE_IMAGE_QUERY);
            boolean useAnd = false;
            for (Map.Entry<String, Object> param: params.entrySet()) {
                if (useAnd) {
                    query.append(" AND ");
                }
                else
                {
                    useAnd = true;
                }
                if (param.getValue() instanceof Long || param.getValue() instanceof Integer)
                {
                    query.append(String.format(IntegerTemplate, param.getKey(), param.getValue()));
                }
                else
                {
                    query.append(String.format(StringTemplate, param.getKey(), param.getValue()));
                }
            }

            try {
                images.addAll(new JdbcTemplate(getImageryDS()).query(query.toString(), null, createRowMapper()));
                LOGGER.debug(String.format("Query: %s ::> %d images", query, images.size()));
            } catch (final SQLException err) {
                LOGGER.error("getImageByQuery() has thrown a SQL Exception", err);
            }
        }
        return images;
    }

    private static String buildQuery(String... clauses) {
        final StringBuilder query = new StringBuilder();
        query.append(QUERY_PARTS.BASE_QUERY.getConstantValue());
        for (String apd: clauses) {
            query.append(" ").append(apd);
        }
        return query.toString();
    }
    
    private NetXImageRowMapper createRowMapper()
    {
    	return new NetXImageRowMapper(
    				String.format("%s:%s", getConfigService().getString("imagingServerScheme"),
    							getConfigService().getString("imagingServerURL")));
    }

    protected abstract DataSourceService getImageryDS();
    
    protected abstract ConfigService getConfigService();
}
