package gov.nga.imaging;

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
