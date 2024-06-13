package org.lamisplus.modules.casemanager.service;

import org.lamisplus.modules.casemanager.domain.CaseManager;
import org.lamisplus.modules.casemanager.dto.CaseManagerDTO;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class CaseManagerDTOMapper implements Function<CaseManager, CaseManagerDTO> {

    @Override
    public CaseManagerDTO apply(CaseManager casemanager) {
        return new CaseManagerDTO(
                casemanager.getId(),
                casemanager.getDesignation(),
                casemanager.getFirstName(),
                casemanager.getLastName(),
                casemanager.getSex(),
                casemanager.getPhoneNumber(),
                casemanager.getUuid(),
                casemanager.getFacilityId(),
                casemanager.getCreateDate(),
                casemanager.getAddress(),
                casemanager.getDateModified(),
                casemanager.getActive(),
                casemanager.getArchived(),
                casemanager.getCreatedBy(),
                casemanager.getModifiedBy(),
                casemanager.getReligion(),
                casemanager.getUsername(),
                casemanager.getPassword(),
                casemanager.getUser_id(),
                casemanager.getPatients()
        );
    }
}
