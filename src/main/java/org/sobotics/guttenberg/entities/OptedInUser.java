package org.sobotics.guttenberg.entities;

/**
 * Created by bhargav.h on 01-Oct-16.
 */
public class OptedInUser {
    SOUser user;
    Long roomId;
    boolean whenInRoom;
    double minScore;

    public SOUser getUser() {
        return user;
    }

    public void setUser(SOUser user) {
        this.user = user;
    }

    public boolean isWhenInRoom() {
        return whenInRoom;
    }

    public void setWhenInRoom(boolean whenInRoom) {
        this.whenInRoom = whenInRoom;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public double getMinScore() {
        return this.minScore;
    }
    
    public void setMinScore(double score) {
        this.minScore = score;
    }

    @Override
    public String toString() {
        return "OptedInUser{" +
                "user=" + user +
                ", roomId=" + roomId +
                ", whenInRoom=" + whenInRoom +
                ", minScore=" + minScore +
                '}';
    }
}
