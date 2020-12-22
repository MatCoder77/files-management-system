package com.awscourse.filesmanagementsystem.infrastructure.jpa;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class LikeClauseUtils {

    private static final CharSequenceTranslator LIKE_WILDCARD_ESCAPE;
    public static final char ESCAPE_CHARACTER = '\\';

    static {
        final Map<CharSequence, CharSequence> escapeMap = new HashMap<>();
        escapeMap.put("%" ,"\\%" );
        escapeMap.put("_" ,"\\_" );
        LIKE_WILDCARD_ESCAPE = new AggregateTranslator(new LookupTranslator(escapeMap));
    }

    public static String escape(final String input) {
        return LIKE_WILDCARD_ESCAPE.translate(input);
    }

    public static String getWrappedInContainsPattern(String text) {
        return '%' + escape(text) + "%";
    }

    public static String getWrappedInStartsWithPattern(String text) {
        return escape(text) + '%';
    }

    public static String getWrappedInEndsWithPattern(String text) {
        return '%' + escape(text);
    }

}
