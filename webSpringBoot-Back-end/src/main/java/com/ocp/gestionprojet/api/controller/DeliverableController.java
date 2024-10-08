package com.ocp.gestionprojet.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ocp.gestionprojet.api.service.interfaces.DeliverableService;

@RestController
@RequestMapping("api/deliverables")

public class DeliverableController {

    @Autowired
    private DeliverableService deliverableService;

    @DeleteMapping("/{id}")
    private ResponseEntity<?> delete(@PathVariable("id") Integer id){
        this.deliverableService.delete(id);
        return ResponseEntity.noContent().build();

    }
    
}
