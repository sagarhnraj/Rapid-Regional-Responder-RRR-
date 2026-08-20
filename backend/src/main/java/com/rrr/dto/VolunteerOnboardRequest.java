package com.rrr.dto;

import java.util.ArrayList;
import java.util.List;

public class VolunteerOnboardRequest {

    private Integer maxRangeMeters = 1000;
    private List<String> skills = new ArrayList<>();
    private Double latitude = 0.0;
    private Double longitude = 0.0;

    public VolunteerOnboardRequest() {}

    public Integer getMaxRangeMeters() { return maxRangeMeters; }
    public void setMaxRangeMeters(Integer maxRangeMeters) { this.maxRangeMeters = maxRangeMeters; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
