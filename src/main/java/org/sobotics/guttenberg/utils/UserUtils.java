/*
 * Copyright (C) 2019 SOBotics (https://sobotics.org) and contributors on GitHub
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

package org.sobotics.guttenberg.utils;

import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;
import org.sobotics.guttenberg.entities.OptedInUser;
import org.sobotics.guttenberg.entities.SOUser;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by bhargav.h on 18-Sep-16.
 */
public class UserUtils {

  public static List<OptedInUser> getUsersOptedIn(double score, long roomId) {
    List<OptedInUser> optedInUsers = new ArrayList<>();

    String filename = FilePathUtils.optedUsersFile;
    try {
      List<String> lines = FileUtils.readFile(filename);
      for (String e : lines) {
        String[] pieces = e.split(",");
        double minScore = new Double(pieces[4]);
        if (Long.valueOf(pieces[2]).equals(roomId) && score >= minScore) {

          OptedInUser optedInUser = new OptedInUser();

          SOUser SOUser = new SOUser();
          SOUser.setUsername(pieces[1].replace("\"", ""));
          SOUser.setUserId(Integer.parseInt(pieces[0]));

          optedInUser.setUser(SOUser);
          optedInUser.setRoomId(Long.valueOf(pieces[2]));
          optedInUser.setWhenInRoom(Boolean.parseBoolean(pieces[3]));
          optedInUser.setMinScore(minScore);


          optedInUsers.add(optedInUser);
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return optedInUsers;
  }


  public static List<OptedInUser> pingUserIfApplicable(double score, long roomId) {
    List<OptedInUser> pingList = new ArrayList<>();
    for (OptedInUser optedInUser : getUsersOptedIn(score, roomId)) {
      if (!(checkIfUserIsInList(pingList, optedInUser)))
        pingList.add(optedInUser);
    }

    return pingList;
  }


  public static boolean checkIfUserIsInList(List<OptedInUser> users, OptedInUser checkUser) {
    if (users.isEmpty()) return false;
    for (OptedInUser user : users) {
      if (user.getUser().getUserId() == checkUser.getUser().getUserId()) {
        return true;
      }
    }
    return false;
  }


  public static boolean checkIfUserInRoom(Room room, int userId) {
    User user = room.getUser(userId);
    return user.isCurrentlyInRoom();
  }

}
