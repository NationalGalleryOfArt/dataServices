/*
    NGA Art Data API: Art Entity for representing prior data records
    of an art object, e.g. a previous attribution or title that was
    assigned.  These are rarely used and there are only about 4500 such
    records exposed through these APIs.

    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package gov.nga.entities.art;

import gov.nga.utils.CollectionUtils;
import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ArtObjectHistoricalData extends ArtEntityImpl {
	
	public static enum HISTORICAL_DATA_TYPE {
		PREVIOUS_ATTRIBUTION, PREVIOUS_TITLE, UNKNOWN;
		
		static Map<String, HISTORICAL_DATA_TYPE> myMap = CollectionUtils.newHashMap();
		
		static {
			myMap.put("previous_attribution",		PREVIOUS_ATTRIBUTION);
			myMap.put("previous_title",				PREVIOUS_TITLE);
		}
		
		public static HISTORICAL_DATA_TYPE dataTypeForLabel(String label) {
			if (label != null) {
				HISTORICAL_DATA_TYPE lookup = myMap.get(label);
				if (lookup != null)
					return lookup;
			}	
			return UNKNOWN;
		}
	}
	
	// private static final Logger log = LoggerFactory.getLogger(ArtObjectHistoricalData.class);
	
	public ArtObjectHistoricalData(ArtDataManagerService manager) {
		super(manager);
	}
	
	protected static final String allHistoricalDataQuery = 
		"SELECT h.fingerprint, h.dataType, h.objectID, h.displayOrder, " +
		" 		h.forwardText, h.invertedText, h.remarks, h.effectiveDate " +
		"FROM 	data.objects_historical_data h ";
	
	public ArtObjectHistoricalData(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,TypeUtils.getLong(rs, 1));
		this.dataType 		= HISTORICAL_DATA_TYPE.dataTypeForLabel(rs.getString(2));
		this.objectID 		= TypeUtils.getLong(rs, 3);
		this.displayOrder 	= TypeUtils.getLong(rs, 4);
		this.forwardText	= rs.getString(5);
		this.invertedText	= rs.getString(6);
		this.remarks		= rs.getString(7);
		this.effectiveDate	= rs.getString(8);
	}
	
	public ArtObjectHistoricalData factory(ResultSet rs) throws SQLException {
		ArtObjectHistoricalData e = new ArtObjectHistoricalData(getManager(),rs);
		return e;
	}
	
	public static Comparator<ArtObjectHistoricalData> sortByEffectiveDateDesc = new Comparator<ArtObjectHistoricalData>() {
		public int compare(ArtObjectHistoricalData a, ArtObjectHistoricalData b) {
			return TypeUtils.compare(b.getEffectiveDate(), a.getEffectiveDate());
		}
	};
	
	public static List<ArtObjectHistoricalData> filterByDataType(List<ArtObjectHistoricalData> list, HISTORICAL_DATA_TYPE dType) {
		List<ArtObjectHistoricalData> newList = CollectionUtils.newArrayList();
		if (list != null) {
			if (dType == null)
				dType = HISTORICAL_DATA_TYPE.UNKNOWN;
			for (ArtObjectHistoricalData h : list) {
				if ( h.getDataType() == dType)
					newList.add(h);
			}
		}
		return newList;
	}
	
    private HISTORICAL_DATA_TYPE dataType;
    public HISTORICAL_DATA_TYPE getDataType() {
    	return dataType;
    }
    
    private Long objectID;
	public Long getObjectID() {
		return objectID;
	}

    private Long displayOrder;
	public Long getDisplayOrder() {
		return displayOrder;
	}

    private String forwardText;
	public String getForwardText() {
		return forwardText;
	}

    private String invertedText;
	public String getInvertedText() {
		return invertedText;
	}

    private String remarks;
	public String getRemarks() {
		return remarks;
	}

    private String effectiveDate;
	public String getEffectiveDate() {
		return effectiveDate;
	}
}
