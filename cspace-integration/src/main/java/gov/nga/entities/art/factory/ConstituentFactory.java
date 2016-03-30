package gov.nga.entities.art.factory;

import gov.nga.entities.art.Constituent;

public interface ConstituentFactory <T>
{
	public T createConstituent(Constituent object);
}
