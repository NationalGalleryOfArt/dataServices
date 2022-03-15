package gov.nga.entities.art.datamanager.data;

import java.util.List;

import gov.nga.common.entities.art.ArtDataSuggestion;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.Media;
import gov.nga.common.entities.art.Place;
import gov.nga.common.entities.art.QueryResult;
import gov.nga.common.entities.art.QueryResultSuggestion;
import gov.nga.common.imaging.NGAImage;
import gov.nga.common.search.ResultsPaginator;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.entities.art.Exhibition;

public class QueryResultFactory 
{
	public static <T extends ArtObject>QueryResultArtData<T> createLocalArtObjectResult(List<T> results)
	{
		return new QueryResultArtDataLocal<T>(results);
	}
	
	public static <T extends ArtObject>QueryResultArtData<T> createLocalArtObjectResult(List<T> results, ResultsPaginator rp)
	{
		return new QueryResultArtDataLocal<T>(results, rp);
	}
	
	public static <T extends Constituent>QueryResultArtData<T> createLocalConstituentResult(List<T> results)
	{
		return new QueryResultArtDataLocal<T>(results);
	}
	
	public static <T extends Constituent>QueryResultArtData<T> createLocalConstituentResult(List<T> results, ResultsPaginator rp)
	{
		return new QueryResultArtDataLocal<T>(results, rp);
	}
	
	public static <T extends Location>QueryResultArtData<T> createLocalLocationResult(List<T> results)
	{
		return new QueryResultArtDataLocal<T>(results);
	}
	
	public static <T extends Exhibition>QueryResultArtData<T> createLocalExhibitionResult(List<T> results)
	{
		return new QueryResultArtDataLocal<T>(results);
	}
	
	public static <T extends Exhibition>QueryResultArtData<T> createLocalExhibitionResult(List<T> results, ResultsPaginator rp)
	{
		return new QueryResultArtDataLocal<T>(results, rp);
	}
	
	public static <T extends Place> QueryResultArtData<T> createLocalPlaceResult(List<T> rslts)
	{
		return new QueryResultArtDataLocal<T>(rslts);
	}
	
	public static <T extends Media> QueryResultArtData<T> createLocalMediaResult(List<T> rslts)
	{
		return new QueryResultArtDataLocal<T>(rslts);
	}
	
	public static <T extends ArtDataSuggestion>QueryResultSuggestion<T> createLocalSuggestionResult(List<T> results)
	{
		return new QueryResultSuggestionLocal<T>(results);
	}
	
	public static <T extends NGAImage>QueryResult<T> createImageResult(List<T> rslts)
	{
		return (QueryResult<T>) new QueryResultNGAImage<T>(rslts);
	}
}
