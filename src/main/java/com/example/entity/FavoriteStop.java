package com.example.entity;
import javax.persistence.*;

@Entity
@Table(name = "favorite_stops")
public class FavoriteStop {

    @Id
    @Column(name = "stop_id", nullable = false)
    private String stopId;

    @Column(name = "stop_name")
    private String stopName;

    public FavoriteStop() {
    }

    public FavoriteStop(String stopId, String stopName) {
        this.stopId = stopId;
        this.stopName = stopName;
    } 

    public String getStopId() {
        return stopId;
    }
    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }
    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    @Override
    public String toString() {
        return "FavoriteStop [stopId=" + stopId + ", stopName=" + stopName + "]";
    }
}