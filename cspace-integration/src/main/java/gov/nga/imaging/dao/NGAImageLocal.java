package gov.nga.imaging.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.imaging.Imagery;
import gov.nga.common.imaging.NGAImage;
import gov.nga.common.utils.CollectionUtils;

public class NGAImageLocal extends NGAImage
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NGAImageLocal.class);

	private final String imageServerURL;

	protected NGAImageLocal(final ResultSet rs, final String serverURL) throws SQLException 
	{
		super(createFieldsMap(rs));
		imageServerURL = serverURL;
	}
	
	private static Map<String, Object> createFieldsMap(final ResultSet rs) throws SQLException
	{
		final Map<String, Object> fieldsMap = CollectionUtils.newHashMap();
		for (String col: NetXImageDAO.PRIMARY_TABLE_COLUMNS) {
            try {
                if (rs.getObject(col) != null) {
                    switch(col) {
                        case "uuid":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "assetname":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "folders":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "iiifformat":
                        	fieldsMap.put(col, Imagery.FORMAT.TIF);
                            break;
                        case "viewtype":
                        	fieldsMap.put(col, Imagery.DISPLAYTYPE.byDbValue(rs.getString(col)));
                            break;
                        case "sequence":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "width":
                        	fieldsMap.put(col, rs.getInt(col));
                            break;
                        case "height":
                        	fieldsMap.put(col, rs.getInt(col));
                            break;
                        case "maxpixels":
                        	fieldsMap.put(col, rs.getInt(col));
                            break;
                        case "authorizations":
                            List<AUTHORIZATION> imgAuths = Collections.EMPTY_LIST;
                            try {
                                JSONArray auths = new JSONArray(rs.getString(col));
                                if (auths.length() > 0) {
                                    imgAuths = CollectionUtils.newArrayList();
                                    for (int idx = 0; idx < auths.length(); idx++) {
                                        AUTHORIZATION ath = AUTHORIZATION.getAuthorizationByValue(auths.getString(idx).toString());
                                        if (ath != null) {
                                            imgAuths.add(ath);
                                        }
                                    }
                                }
                            } catch(final JSONException err) {
                                LOGGER.error("Caught exceptions processing authorizations for image", err);
                            }
                            fieldsMap.put(col, imgAuths);
                            break;
                        case "ispublic":
                        	fieldsMap.put(col, rs.getBoolean(col));
                            break;
                        case "created":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "modified":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "assistivetext":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "iiifsizeinbytes":
                        	fieldsMap.put(col, rs.getLong(col));
                            break;
                        case "depictstmsobjectid":
                        	fieldsMap.put(col, rs.getLong(col));
                            break;
                        case "ri_altmedium":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_altimageref":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_qualifier":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_isdetail":
                        	fieldsMap.put(col, Boolean.parseBoolean(rs.getString(col)));
                            break;
                        case "ri_entitytype":
                        	fieldsMap.put(col, Imagery.ENTITY_TYPE.byDbValue(rs.getString(col)));
                            break;
                        case "ri_entityid":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_photocredit":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_altdisplaydate":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_alttitle":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_projectid":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_viewsubtype":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_altattribution":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_altcreditline":
                        	fieldsMap.put(col, rs.getString(col));
                            break;
                        case "ri_relatedtmsobjectid":
                        	fieldsMap.put(col, rs.getLong(col));
                            break;
                        case "ri_iszoomable":
                        	fieldsMap.put(col, rs.getBoolean(col));
                            break;
                        case "iiiffilesizeinbytes":
                            //do nothing
                            break;
                        default:
                            LOGGER.warn(String.format("No creation action set for column: %s", col));
                    }
                }
                else {
                    //LOGGER.debug(String.format("No data for column %s[%s]", col, rs));
                }
            } catch (final Exception err) {
                LOGGER.warn(String.format("Caught exception processing column: %s", col), err);
            }
        }
		return fieldsMap;
	}

	@Override
	public String getImagingServerURL() 
	{
		return imageServerURL;
	}

}
