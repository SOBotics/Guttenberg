/*
 * Copyright (C) 2019 SOBotics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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
