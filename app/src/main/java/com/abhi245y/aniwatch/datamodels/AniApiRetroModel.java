package com.abhi245y.aniwatch.datamodels;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AniApiRetroModel {

    private ArrayList<SearchResultBean> search_result;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SearchResultBean {
        private String anime_name;
        private String gogo_id;
        private String poster_link;
        private String total_ep;
    }
}
