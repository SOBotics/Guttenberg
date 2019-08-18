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

import com.google.gson.JsonObject;

/**
 * Created by bhargav.h on 01-Oct-16.
 */
public class SOUser {
  private String username;
  private int userId;
  private long reputation;
  private String userType;


  public String getUsername() {
    return username;
  }


  public void setUsername(String username) {
    this.username = username;
  }


  public int getUserId() {
    return userId;
  }


  public void setUserId(int userId) {
    this.userId = userId;
  }


  public long getReputation() {
    return reputation;
  }


  public void setReputation(long reputation) {
    this.reputation = reputation;
  }


  public String getUserType() {
    return userType;
  }


  public void setUserType(String userType) {
    this.userType = userType;
  }


  @Override
  public String toString() {
    return getJson().toString();
  }


  public JsonObject getJson() {
    JsonObject json = new JsonObject();
    json.addProperty("username", username);
    json.addProperty("userId", userId);
    json.addProperty("reputation", reputation);
    json.addProperty("userType", userType);
    return json;
  }
}
