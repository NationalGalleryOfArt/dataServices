package gov.nga.entities.art;

import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.measure.Measure;
import javax.measure.converter.ConversionException;
import javax.measure.quantity.*;
import javax.measure.unit.*;

public class ArtObjectDimension extends ArtEntityImpl {
	
//	private static final Logger log = LoggerFactory.getLogger(ArtObjectDimension.class);

	private static enum UNIT_NAMES {
		_CENTIMETERS("centimeters",SI.CENTIMETER),
		_INCHES     ("inches",     NonSI.INCH),
		_POUNDS     ("pounds",     NonSI.POUND),
		_TONS		("tons",       NonSI.TON_US),
		_KILOGRAMS  ("kilograms",  SI.KILOGRAM),
		_GRAMS      ("grams",      SI.GRAM);
		private String label;
		
		// unit is used to define the type of quantity (length, mass, force, etc.) 
		// associated with this dimension measurement
		private Unit<? extends Quantity> unit = null;
		private UNIT_NAMES(String label, Unit<? extends Quantity> unit) {
			this.label = label;
			this.unit = unit;
		}

		String getLabel() {
			return label;
		}
		
		private static UNIT_NAMES toUnitName(String un) {
			if (un == null)
				return null;
			for (UNIT_NAMES i : UNIT_NAMES.values()) {
				if (i.getLabel().equalsIgnoreCase(un))
					return i;
			}
			return null;
		}
		
		Unit<? extends Quantity> getUnit() {
			return unit;
		}
	}

	public static enum DIMENSION_TYPE {
		WIDTH("width",   Dimension.LENGTH),
		HEIGHT("height", Dimension.LENGTH),
		DEPTH("depth",   Dimension.LENGTH);

		private String label = null;
		
		// unit is used to define the type of quantity (length, mass, force, etc.) 
		// associated with this dimension measurement
		private Dimension dim;

		private DIMENSION_TYPE(String label, Dimension dim) {
			this.label = label;
			this.dim = dim;
		}

		public String getLabel() {
			return label;
		}
		
		private static DIMENSION_TYPE toDimensionType(String dt) {
			if (dt == null)
				return null;
			for (DIMENSION_TYPE i : DIMENSION_TYPE.values()) {
				if (i.getLabel().equalsIgnoreCase(dt))
					return i;
			}
			return null;
		}
		
		private Dimension getDimension() {
			return dim;
		}
	}
	
	ArtObjectDimension(ArtDataManagerService manager) {
		super(manager);
	}
	
	static final String allObjectsDimensionsQuery = 
		"SELECT d.dimensionID, d.objectID, d.element, d.dimensionType, d.dimension, d.unitName " +
		"FROM data.objects_dimensions d " +
		"ORDER BY d.objectID, d.dimensionID ";
	
	ArtObjectDimension(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		// we did not generate a data finger print for object dimensions - if we find we need to do that
		// it's easy enough to add later
		super(manager);
		dimensionID 	= TypeUtils.getLong(rs, 1);
		objectID 		= TypeUtils.getLong(rs, 2);
		element 		= rs.getString(3);
		setDimension(rs.getString(4), TypeUtils.getDouble(rs, 5), rs.getString(6));
	}
	
	public ArtObjectDimension factory(ResultSet rs) throws SQLException {
		return new ArtObjectDimension(getManager(), rs);
	}
	
	private Long objectID;
	Long getObjectID() {
		return objectID;
	}

	private Long dimensionID;
	Long getDimensionID() {
		return dimensionID;
	}

	private Measure<Double, ? extends Quantity> dimension;
	private void setDimension(String dimensionType, Double dimension, String unitName) {
		DIMENSION_TYPE dt = DIMENSION_TYPE.toDimensionType(dimensionType);
		this.dimensionType = dt;
		if (dt != null) {
			// we understand the type of dimension (width, height, depth)
			setUnit(unitName);
			if (unit != null && unit.getDimension() == dt.getDimension()) {
				// if units are defined and compatible with the TMS dimension type 
				// then everyone is on the same page, so we go ahead and assign the value
			    this.dimension = Measure.valueOf(dimension, getUnit());
			}
		}	
	}
	
	@SuppressWarnings("unchecked")
	<T extends Quantity> Measure<Double, T> getDimension() {
		return (Measure<Double, T>) this.dimension;
	}
	
	private String element;
	String getElement() {
		return element;
	}

	private DIMENSION_TYPE dimensionType;
	DIMENSION_TYPE getDimensionType() {
		return dimensionType;
	}
	
	private Unit<? extends Quantity> unit;
	Unit<? extends Quantity> getUnit() {
		return unit;
	}
	private void setUnit(String unitName) {
		UNIT_NAMES un = UNIT_NAMES.toUnitName(unitName);
		if (un == null)
			this.unit = null;
		else
			// we support this unit of measure
			this.unit = un.getUnit();
	}

	public static <T extends Quantity> Measure<Double, T> findDimension(List<ArtObjectDimension> dimensions, DIMENSION_TYPE dType) {
		if (dType == null || dimensions == null)
			return null;
		for (ArtObjectDimension d : dimensions) {
			if (dType == d.getDimensionType() ) {
				Measure<Double, T> dim = d.getDimension();
				if (dim != null) {
					// we have a dimension!
					Unit<T> u = dim.getUnit();
					if (u != null && u.isCompatible(d.getUnit()))
						// we have a dimension compatible with the original data
						// note that this might not be consistent with the  
						return d.getDimension();
				}
				// if the dimensions are not compatible the caller probably used the wrong class of units for the dimension
				throw new ConversionException("The requested dimension was found but incompatible quantity type was requested");
			}
		}
		return null;
	}

}
