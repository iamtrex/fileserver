package com.rweqx.files;

import com.rweqx.sql.SecureStore;
import com.rweqx.types.User;

import javax.inject.Inject;
import java.util.List;

public class UserManagementService {

    @Inject
    private SecureStore secureStore;


    public List<User> getUsers() {
        return secureStore.getAllUsers();
    }

}
