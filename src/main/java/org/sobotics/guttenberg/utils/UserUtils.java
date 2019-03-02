package org.sobotics.guttenberg.utils;

import java.util.ArrayList;
import java.util.List;

import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;
import org.sobotics.guttenberg.entities.OptedInUser;
import org.sobotics.guttenberg.entities.SOUser;


/**
 * Created by bhargav.h on 18-Sep-16.
 */
public class UserUtils {

    public static List<OptedInUser> getUsersOptedIn(double score, long roomId){
        List <OptedInUser> optedInUsers = new ArrayList<>();

        String filename = FilePathUtils.optedUsersFile;
        try {
            List<String> lines = FileUtils.readFile(filename);
            for(String e:lines){
                String pieces[] = e.split(",");
                double minScore = new Double(pieces[4]);
                if(Long.valueOf(pieces[2]).equals(Long.valueOf(roomId)) && score >= minScore){

                    OptedInUser optedInUser = new OptedInUser();

                    SOUser SOUser = new SOUser();
                    SOUser.setUsername(pieces[1].replace("\"",""));
                    SOUser.setUserId(Integer.parseInt(pieces[0]));

                    optedInUser.setUser(SOUser);
                    optedInUser.setRoomId(Long.valueOf(pieces[2]));
                    optedInUser.setWhenInRoom(Boolean.parseBoolean(pieces[3]));
                    optedInUser.setMinScore(minScore);


                    optedInUsers.add(optedInUser);
                }
            }
        }
        catch (Throwable e){
            e.printStackTrace();
        }
        return optedInUsers;
    }

    public static List<OptedInUser> pingUserIfApplicable(double score, long roomId)
    {
        List<OptedInUser> pingList = new ArrayList<>();
        for(OptedInUser optedInUser :getUsersOptedIn(score,roomId)) {
            if(!(checkIfUserIsInList(pingList,optedInUser)))
                pingList.add(optedInUser);
        }
        
        return pingList;
    }

    public static boolean checkIfUserIsInList(List<OptedInUser> users, OptedInUser checkUser){
        if(users.size()==0) return false;
        for(OptedInUser user:users){
            if(user.getUser().getUserId() == checkUser.getUser().getUserId()){
                return true;
            }
        }
        return false;
    }

    public static boolean checkIfUserInRoom(Room room, int userId){
        User user = room.getUser(userId);
        return user.isCurrentlyInRoom();
    }

}
