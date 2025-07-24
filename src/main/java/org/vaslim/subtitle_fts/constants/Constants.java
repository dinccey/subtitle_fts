package org.vaslim.subtitle_fts.constants;

import java.util.List;

public interface Constants
{
    interface Endpoint{
        String ALL_PATHS = "/**";
    }

    interface SecurityConstants {
        String SECRET = "SECRET_KEY";
        long EXPIRATION_TIME = 900_000; // 15 mins
        String TOKEN_PREFIX = "Bearer ";
        String HEADER_STRING = "Authorization";
    }

    String INDEX_SUBTITLES= "subtitles";
    String INDEX_CATEGORY_INFO = "category_info";
}
