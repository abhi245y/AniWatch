package com.abhi245y.aniwatch.datamodels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class KitsuApiSearchModel {

    private List<DataBean> data;
    private MetaBean meta;
    private LinksBean links;

    public static KitsuApiSearchModel objectFromData(String str) {

        return new Gson().fromJson(str, KitsuApiSearchModel.class);
    }

    @NoArgsConstructor
    @Data
    public static class MetaBean {
        public static MetaBean objectFromData(String str) {

            return new Gson().fromJson(str, MetaBean.class);
        }
    }

    @NoArgsConstructor
    @Data
    public static class LinksBean {
        public static LinksBean objectFromData(String str) {

            return new Gson().fromJson(str, LinksBean.class);
        }
    }

    @NoArgsConstructor
    @Data
    public static class DataBean {
        private String id;
        private String type;
        private LinksBean links;
        private AttributesBean attributes;
        private RelationshipsBean relationships;

        public static DataBean objectFromData(String str) {

            return new Gson().fromJson(str, DataBean.class);
        }

        @NoArgsConstructor
        @Data
        public static class LinksBean {
            private String self;

            public static LinksBean objectFromData(String str) {

                return new Gson().fromJson(str, LinksBean.class);
            }
        }

        @NoArgsConstructor
        @Data
        public static class AttributesBean {
            private String createdAt;
            private String updatedAt;
            private String slug;
            private String synopsis;
            private String description;
            private int coverImageTopOffset;
            private TitlesBean titles;
            private String canonicalTitle;
            private List<String> abbreviatedTitles;
            private String averageRating;
            private RatingFrequenciesBean ratingFrequencies;
            private int userCount;
            private int favoritesCount;
            private String startDate;
            private String endDate;
            private Object nextRelease;
            private int popularityRank;
            private int ratingRank;
            private String ageRating;
            private String ageRatingGuide;
            private String subtype;
            private String status;
            private String tba;
            private PosterImageBean posterImage;
            private CoverImageBean coverImage;
            private int episodeCount;
            private int episodeLength;
            private int totalLength;
            private String youtubeVideoId;
            private String showType;
            private boolean nsfw;

            public static AttributesBean objectFromData(String str) {

                return new Gson().fromJson(str, AttributesBean.class);
            }

            @NoArgsConstructor
            @Data
            public static class TitlesBean {
                private String en;
                private String en_jp;
                private String en_us;
                private String ja_jp;

                public static TitlesBean objectFromData(String str) {

                    return new Gson().fromJson(str, TitlesBean.class);
                }
            }

            @NoArgsConstructor
            @Data
            public static class RatingFrequenciesBean {
                @SerializedName("2")
                private String _$2;
                @SerializedName("3")
                private String _$3;
                @SerializedName("4")
                private String _$4;
                @SerializedName("5")
                private String _$5;
                @SerializedName("6")
                private String _$6;
                @SerializedName("7")
                private String _$7;
                @SerializedName("8")
                private String _$8;
                @SerializedName("9")
                private String _$9;
                @SerializedName("10")
                private String _$10;
                @SerializedName("11")
                private String _$11;
                @SerializedName("12")
                private String _$12;
                @SerializedName("13")
                private String _$13;
                @SerializedName("14")
                private String _$14;
                @SerializedName("15")
                private String _$15;
                @SerializedName("16")
                private String _$16;
                @SerializedName("17")
                private String _$17;
                @SerializedName("18")
                private String _$18;
                @SerializedName("19")
                private String _$19;
                @SerializedName("20")
                private String _$20;

                public static RatingFrequenciesBean objectFromData(String str) {

                    return new Gson().fromJson(str, RatingFrequenciesBean.class);
                }
            }

            @NoArgsConstructor
            @Data
            public static class PosterImageBean {
                private String tiny;
                private String small;
                private String medium;
                private String large;
                private String original;
                private MetaBean meta;

                public static PosterImageBean objectFromData(String str) {

                    return new Gson().fromJson(str, PosterImageBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class MetaBean {
                    private DimensionsBean dimensions;

                    public static MetaBean objectFromData(String str) {

                        return new Gson().fromJson(str, MetaBean.class);
                    }

                    @NoArgsConstructor
                    @Data
                    public static class DimensionsBean {
                        private TinyBean tiny;
                        private SmallBean small;
                        private MediumBean medium;
                        private LargeBean large;

                        public static DimensionsBean objectFromData(String str) {

                            return new Gson().fromJson(str, DimensionsBean.class);
                        }

                        @NoArgsConstructor
                        @Data
                        public static class TinyBean {
                            private int width;
                            private int height;

                            public static TinyBean objectFromData(String str) {

                                return new Gson().fromJson(str, TinyBean.class);
                            }
                        }

                        @NoArgsConstructor
                        @Data
                        public static class SmallBean {
                            private int width;
                            private int height;

                            public static SmallBean objectFromData(String str) {

                                return new Gson().fromJson(str, SmallBean.class);
                            }
                        }

                        @NoArgsConstructor
                        @Data
                        public static class MediumBean {
                            private int width;
                            private int height;

                            public static MediumBean objectFromData(String str) {

                                return new Gson().fromJson(str, MediumBean.class);
                            }
                        }

                        @NoArgsConstructor
                        @Data
                        public static class LargeBean {
                            private int width;
                            private int height;

                            public static LargeBean objectFromData(String str) {

                                return new Gson().fromJson(str, LargeBean.class);
                            }
                        }
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class CoverImageBean {
                private String tiny;
                private String small;
                private String large;
                private String original;
                private MetaBean meta;

                public static CoverImageBean objectFromData(String str) {

                    return new Gson().fromJson(str, CoverImageBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class MetaBean {
                    private DimensionsBean dimensions;

                    public static MetaBean objectFromData(String str) {

                        return new Gson().fromJson(str, MetaBean.class);
                    }

                    @NoArgsConstructor
                    @Data
                    public static class DimensionsBean {
                        private TinyBean tiny;
                        private SmallBean small;
                        private LargeBean large;

                        public static DimensionsBean objectFromData(String str) {

                            return new Gson().fromJson(str, DimensionsBean.class);
                        }

                        @NoArgsConstructor
                        @Data
                        public static class TinyBean {
                            private int width;
                            private int height;

                            public static TinyBean objectFromData(String str) {

                                return new Gson().fromJson(str, TinyBean.class);
                            }
                        }

                        @NoArgsConstructor
                        @Data
                        public static class SmallBean {
                            private int width;
                            private int height;

                            public static SmallBean objectFromData(String str) {

                                return new Gson().fromJson(str, SmallBean.class);
                            }
                        }

                        @NoArgsConstructor
                        @Data
                        public static class LargeBean {
                            private int width;
                            private int height;

                            public static LargeBean objectFromData(String str) {

                                return new Gson().fromJson(str, LargeBean.class);
                            }
                        }
                    }
                }
            }
        }

        @NoArgsConstructor
        @Data
        public static class RelationshipsBean {
            private GenresBean genres;
            private CategoriesBean categories;
            private CastingsBean castings;
            private InstallmentsBean installments;
            private MappingsBean mappings;
            private ReviewsBean reviews;
            private MediaRelationshipsBean mediaRelationships;
            private CharactersBean characters;
            private StaffBean staff;
            private ProductionsBean productions;
            private QuotesBean quotes;
            private EpisodesBean episodes;
            private StreamingLinksBean streamingLinks;
            private AnimeProductionsBean animeProductions;
            private AnimeCharactersBean animeCharacters;
            private AnimeStaffBean animeStaff;

            public static RelationshipsBean objectFromData(String str) {

                return new Gson().fromJson(str, RelationshipsBean.class);
            }

            @NoArgsConstructor
            @Data
            public static class GenresBean {
                private LinksBean links;

                public static GenresBean objectFromData(String str) {

                    return new Gson().fromJson(str, GenresBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class CategoriesBean {
                private LinksBean links;

                public static CategoriesBean objectFromData(String str) {

                    return new Gson().fromJson(str, CategoriesBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class CastingsBean {
                private LinksBean links;

                public static CastingsBean objectFromData(String str) {

                    return new Gson().fromJson(str, CastingsBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class InstallmentsBean {
                private LinksBean links;

                public static InstallmentsBean objectFromData(String str) {

                    return new Gson().fromJson(str, InstallmentsBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class MappingsBean {
                private LinksBean links;

                public static MappingsBean objectFromData(String str) {

                    return new Gson().fromJson(str, MappingsBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class ReviewsBean {
                private LinksBean links;

                public static ReviewsBean objectFromData(String str) {

                    return new Gson().fromJson(str, ReviewsBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class MediaRelationshipsBean {
                private LinksBean links;

                public static MediaRelationshipsBean objectFromData(String str) {

                    return new Gson().fromJson(str, MediaRelationshipsBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class CharactersBean {
                private LinksBean links;

                public static CharactersBean objectFromData(String str) {

                    return new Gson().fromJson(str, CharactersBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class StaffBean {
                private LinksBean links;

                public static StaffBean objectFromData(String str) {

                    return new Gson().fromJson(str, StaffBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class ProductionsBean {
                private LinksBean links;

                public static ProductionsBean objectFromData(String str) {

                    return new Gson().fromJson(str, ProductionsBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class QuotesBean {
                private LinksBean links;

                public static QuotesBean objectFromData(String str) {

                    return new Gson().fromJson(str, QuotesBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class EpisodesBean {
                private LinksBean links;

                public static EpisodesBean objectFromData(String str) {

                    return new Gson().fromJson(str, EpisodesBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class StreamingLinksBean {
                private LinksBean links;

                public static StreamingLinksBean objectFromData(String str) {

                    return new Gson().fromJson(str, StreamingLinksBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class AnimeProductionsBean {
                private LinksBean links;

                public static AnimeProductionsBean objectFromData(String str) {

                    return new Gson().fromJson(str, AnimeProductionsBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class AnimeCharactersBean {
                private LinksBean links;

                public static AnimeCharactersBean objectFromData(String str) {

                    return new Gson().fromJson(str, AnimeCharactersBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }

            @NoArgsConstructor
            @Data
            public static class AnimeStaffBean {
                private LinksBean links;

                public static AnimeStaffBean objectFromData(String str) {

                    return new Gson().fromJson(str, AnimeStaffBean.class);
                }

                @NoArgsConstructor
                @Data
                public static class LinksBean {
                    private String self;
                    private String related;

                    public static LinksBean objectFromData(String str) {

                        return new Gson().fromJson(str, LinksBean.class);
                    }
                }
            }
        }
    }
}
