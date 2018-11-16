// TODO - refactor ALL of the image classes in both the Art Data APIs and the AEM system to:
// 1. eliminate hard coded strings to project names
// 2. eliminate the concept of "Derivatives" now that all of our web images are IIIF enabled

/*
    NGA Art Data API: Imagery is a class specifically designed to interface with the NGA's
    web site content management system and is unfortunately currently bound via string literals
    to particular project names, each of which have a set of images in the NGA's imaging systems.

    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: NGA Contractors

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
*/package gov.nga.imaging;

/**
 * Constants for image API
 */

public class Imagery
{

    public static enum PROJECT
    {
        ACCADEMIA("accademia_di_san_luca");
        private String dbValue;

        PROJECT(String dbValue)
        {
            this.dbValue = dbValue;
        }

        public String getDbValue()
        {
            return dbValue;
        }
        
        public static PROJECT byDbValue(String dbValue)
        {
            for (PROJECT item: PROJECT.values())
                if (item.dbValue.equals(dbValue))
                    return item;

            return null;
        }
    }

    public static enum ENTITY_TYPE
    {
        DOCUMENT("document");
        private String dbValue;

        ENTITY_TYPE(String dbValue)
        {
            this.dbValue = dbValue;
        }

        public String getDbValue()
        {
            return dbValue;
        }

        public static ENTITY_TYPE byDbValue(String dbValue)
        {
            for (ENTITY_TYPE item: ENTITY_TYPE.values())
                if (item.dbValue.equals(dbValue))
                    return item;

            return null;
        }
        
    }
    
    public static enum DISPLAYTYPE
    {
        PRIMARY("primary"),
        CROPPED("crop"),
        ALTERNATE("alternate"),
        COMPFIG("compfig"),
        TECHNICAL("technical"),
        INSCRIPTION("inscription");

        private String dbValue;

        DISPLAYTYPE(String dbValue)
        {
            this.dbValue = dbValue;
        }

        public String getDbValue()
        {
            return dbValue;
        }

        public static DISPLAYTYPE byDbValue(String dbValue)
        {
            for (DISPLAYTYPE item: DISPLAYTYPE.values())
                if (item.dbValue.equals(dbValue))
                    return item;

            return null;
        }

    }

    public static enum FORMAT
    {
        PTIF("PTIF"),
        JPEG("JPEG");

        private String dbValue;

        FORMAT(String dbValue)
        {
            this.dbValue = dbValue;
        }

        public String getDbValue()
        {
            return dbValue;
        }

        public static FORMAT byDbValue(String dbValue)
        {
            for (FORMAT item: FORMAT.values())
                if (item.dbValue.equals(dbValue))
                    return item;

            return null;
        }

    }

}
