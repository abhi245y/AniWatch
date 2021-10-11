package com.abhi245y.aniwatch.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SearchViewModel implements Parcelable {

    AnimeMongo animeMongo;


    protected SearchViewModel(Parcel in) {
    }

    public static final Creator<SearchViewModel> CREATOR = new Creator<SearchViewModel>() {
        @Override
        public SearchViewModel createFromParcel(Parcel in) {
            return new SearchViewModel(in);
        }

        @Override
        public SearchViewModel[] newArray(int size) {
            return new SearchViewModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
