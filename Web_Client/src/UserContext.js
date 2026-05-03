import React, {createContext, useState, useEffect } from 'react';
// import users from "./data/db.json"

export const UserContext = createContext();

export const UserProvider = ({children}) => {
  return (
        <UserContext.Provider >
            {children}
        </UserContext.Provider>
  );  
};


