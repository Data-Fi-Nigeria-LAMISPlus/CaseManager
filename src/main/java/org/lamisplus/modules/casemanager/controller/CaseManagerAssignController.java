package org.lamisplus.modules.casemanager.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.casemanager.domain.AssignedPatient;
import org.lamisplus.modules.casemanager.dto.AssignCaseManagerDTO;
import org.lamisplus.modules.casemanager.service.AssignCaseMangerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/assign")
public class CaseManagerAssignController {
    private final AssignCaseMangerService assignCaseMangerService;

    @PostMapping("/create")
    public List<AssignedPatient> SaveAssignedPatients(@RequestBody AssignCaseManagerDTO assignCaseManagerDTO) throws Exception {
        return assignCaseMangerService.save(assignCaseManagerDTO);
    }

    @GetMapping("/list")
    public List<AssignedPatient> getAllAssigned() {
        return assignCaseMangerService.findAll();
    }

    @PostMapping("/reassign")
    public List<AssignedPatient> ReassignPatients(@RequestBody AssignCaseManagerDTO assignCaseManagerDTO) throws Exception {
        return assignCaseMangerService.reassign(assignCaseManagerDTO);
    }
    @DeleteMapping("/delete/{id}")
    public String DeleteCaseManager(@PathVariable int id) throws Exception {
        return assignCaseMangerService.delete(id);
    }
    @GetMapping("/casemanager-patients/{userId}")
    public List<AssignedPatient> getAllAssignedPatients(@PathVariable String userId) {
        return assignCaseMangerService.getAllAssignedByUserid(userId);
    }
}
