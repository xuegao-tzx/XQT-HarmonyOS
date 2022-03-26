package com.xcl.location.Net;

import java.io.Serializable;

/**
 * The type Dxjx.
 *
 * @author Xcl
 * @date 2022 /3/25
 * @package com.xcl.location.Net
 */
public class DXJX {
    /**
     * The Result.
     */
    public Result result;

    /**
     * The type Result.
     */
    public static class Result implements Serializable {
        /**
         * The Address component.
         */
        public AddressComponent addressComponent;

        /**
         * The type Address component.
         */
        public static class AddressComponent implements Serializable {
            /**
             * The Province.
             */
            public String province;
            /**
             * The City.
             */
            public String city;
            /**
             * The District.
             */
            public String district;
            /**
             * The Street.
             */
            public String street;
        }
    }
}
