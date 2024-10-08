package com.ocp.gestionprojet.api.service.interfaces;

import com.ocp.gestionprojet.api.exception.EntityNotFoundException;
import com.ocp.gestionprojet.api.model.dto.reportDto.ReportDto;
import java.util.List;

public interface ReportService {

      // Ajouter un rapport
    ReportDto addReport(ReportDto reportDto) throws EntityNotFoundException;

    // Supprimer un rapport par ID
    void deleteReport(Integer id) throws EntityNotFoundException;

    List <ReportDto> findReportByMembre(Integer id);

    
}
