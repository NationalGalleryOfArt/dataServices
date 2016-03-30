package gov.nga.entities.art.factory;

import gov.nga.entities.art.ArtObject;

public interface ArtObjectFactory<T extends ArtObject>
{
	public T createArtObject(ArtObject object);
}
