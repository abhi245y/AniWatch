package com.abhi245y.aniwatch.datamodels;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RecentReleasedAniViewModel {
    AnimeMongo animeMongo;
    String current_ep;

}
