package com.ocp.gestionprojet.api.exception;

public class EntityNotFoundException extends Exception {

    public EntityNotFoundException(String message){
        super(message);
    }


    public String GetDetaillMessage(){
        return this.getMessage();
    }
    
}
