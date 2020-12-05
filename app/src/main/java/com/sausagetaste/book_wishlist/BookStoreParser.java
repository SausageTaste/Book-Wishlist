package com.sausagetaste.book_wishlist;

import android.util.Log;


public class BookStoreParser {

    //// Definitions

    private enum Company {
        ridibooks,
        unknown,
    }

    interface Parser_HTML {
        String find_title();
        String find_cover_url();
        double find_rating_normalized();
        String find_isbn();
        String find_description();
    }

    private static class Parser_Ridibooks implements Parser_HTML {

        protected final String html;

        Parser_Ridibooks(final String html) {
            this.html = html;
        }

        @Override
        public String find_title() {
            return this.parse_content("og:title");
        }

        @Override
        public String find_cover_url() {
            return this.parse_content("og:image");
        }

        @Override
        public double find_rating_normalized() {
            final String found = this.parse_content("rating:normalized_value");
            if (null == found) {
                return -1;
            }

            try {
                return Double.parseDouble(found);
            }
            catch (Exception e) {
                return -1;
            }
        }

        @Override
        public String find_isbn() {
            return this.parse_content("books:isbn");
        }

        @Override
        public String find_description() {
            return this.parse_content("og:description");
        }

        private String parse_content(final String pre_content_id) {
            final String CONTENT_TAG = "content=\"";

            final int property_index = this.html.indexOf(pre_content_id);
            if (-1 == property_index) {
                return null;
            }

            int content_index = this.html.indexOf(CONTENT_TAG, property_index);
            if (-1 == content_index) {
                return null;
            }

            final int head = content_index + CONTENT_TAG.length();
            final int tail = this.html.indexOf("\"", head);
            return this.html.substring(head, tail);
        }

    }


    //// Methods

    public static Parser_HTML select_html_parser(final String html) {
        Company company = detect_company(html);

        switch (company) {

        case ridibooks:
            Log.v("select_html_parser", html);
            return new Parser_Ridibooks(html);
        case unknown:
        default:
            return null;

        }
    }

    private static Company detect_company(final String html) {
        int index_of_ridi = html.indexOf("ridibooks");
        if (-1 != index_of_ridi) {
            return Company.ridibooks;
        }

        return Company.unknown;
    }

}
