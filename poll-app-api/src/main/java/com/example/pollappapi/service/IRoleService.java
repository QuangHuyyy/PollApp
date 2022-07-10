package com.example.pollappapi.service;

import com.example.pollappapi.model.ERoleName;
import com.example.pollappapi.model.Role;

public interface IRoleService extends IGeneralService<Role>{
    Role findByName(ERoleName eRoleName);
}
