package com.abhi245y.aniwatch.datamodels;

import com.google.gson.Gson;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AniApiRecentListModel {


    private List<RecentReleasesBean> recent_releases;

    public static AniApiRecentListModel objectFromData(String str) {

        return new Gson().fromJson(str, AniApiRecentListModel.class);
    }

    @NoArgsConstructor
    @Data
    public static class RecentReleasesBean {
        private String ep_link;
        private String ep_num;
        private String img_link;
        private String title;

        public static RecentReleasesBean objectFromData(String str) {

            return new Gson().fromJson(str, RecentReleasesBean.class);
        }
    }
}
