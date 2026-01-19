package com.sixdee.text2rule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ScheduleParserResult {
    @JsonProperty("schedule_type")
    private String scheduleType;

    @JsonProperty("repeat")
    private String repeat;

    @JsonProperty("last_fetch")
    private String lastFetch;

    @JsonProperty("segment_rule_start_date")
    private String segmentRuleStartDate;

    @JsonProperty("segment_rule_end_date")
    private String segmentRuleEndDate;

    @JsonProperty("interval")
    private String interval;

    @JsonProperty("frequency")
    private String frequency;

    @JsonProperty("hours")
    private String hours;

    @JsonProperty("minutes")
    private String minutes;

    @JsonProperty("type")
    private String type;

    @JsonProperty("period")
    private String period;

    @JsonProperty("week")
    private String week;

    @JsonProperty("day")
    private String day;

    @JsonProperty("select_days")
    private List<Object> selectDays; // Can be String or Integer

    @JsonProperty("start_time")
    private Map<String, String> startTime;

    @JsonProperty("end_time")
    private Map<String, String> endTime;

    // Getters and Setters
    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getLastFetch() {
        return lastFetch;
    }

    public void setLastFetch(String lastFetch) {
        this.lastFetch = lastFetch;
    }

    public String getSegmentRuleStartDate() {
        return segmentRuleStartDate;
    }

    public void setSegmentRuleStartDate(String segmentRuleStartDate) {
        this.segmentRuleStartDate = segmentRuleStartDate;
    }

    public String getSegmentRuleEndDate() {
        return segmentRuleEndDate;
    }

    public void setSegmentRuleEndDate(String segmentRuleEndDate) {
        this.segmentRuleEndDate = segmentRuleEndDate;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<Object> getSelectDays() {
        return selectDays;
    }

    public void setSelectDays(List<Object> selectDays) {
        this.selectDays = selectDays;
    }

    public Map<String, String> getStartTime() {
        return startTime;
    }

    public void setStartTime(Map<String, String> startTime) {
        this.startTime = startTime;
    }

    public Map<String, String> getEndTime() {
        return endTime;
    }

    public void setEndTime(Map<String, String> endTime) {
        this.endTime = endTime;
    }
}
