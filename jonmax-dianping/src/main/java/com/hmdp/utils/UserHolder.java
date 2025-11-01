package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;

public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUserDTO(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUserDTO(){
        return tl.get();
    }

    public static void removeUserDTO(){
        tl.remove();
    }

    private static final ThreadLocal<User> t2 = new ThreadLocal<>();

    public static void saveUserEntity(User user){
        t2.set(user);
    }

    public static User getUserEntity(){
        return t2.get();
    }

    public static void removeUserEntity(){
        t2.remove();
    }
}
