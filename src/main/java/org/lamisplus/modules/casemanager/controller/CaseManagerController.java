package org.lamisplus.modules.casemanager.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.casemanager.domain.CaseManager;
import org.lamisplus.modules.casemanager.dto.CaseManagerDTO;
import org.lamisplus.modules.casemanager.dto.CaseManagerRequest;
import org.lamisplus.modules.casemanager.dto.PatientListDTO;
import org.lamisplus.modules.casemanager.service.CaseManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/casemanager")
public class CaseManagerController {
    private final CaseManagerService caseManagerService;

    @PostMapping("/create")
    public CaseManager SaveCaseManger(@RequestBody CaseManagerRequest caseManagerRequest) throws Exception {
        return caseManagerService.Save(caseManagerRequest);
    }

    @PutMapping("/update/{id}")
    public CaseManager UpdateCaseManger(@PathVariable int id, @RequestBody CaseManagerRequest caseManagerRequest) throws Exception {
        return caseManagerService.Update(caseManagerRequest, id);
    }

    @GetMapping("/patients/{facilityId}")
    public ResponseEntity<List<PatientListDTO>> GetPatientsForCaseManagerAssignment(
            @PathVariable("facilityId") Long facilityId,
            @RequestParam(value = "stateOfResidence", required = false)  Integer stateOfResidence,
            @RequestParam(value = "lgaOfResidence", required = false)  Integer lgaOfResidence
//            @RequestParam(value = "gender",  defaultValue = "", required = false)  String gender,
//            @RequestParam(value = "targetGroup",  defaultValue = "", required = false)  String targetGroup
    ){
        return  ResponseEntity.ok(caseManagerService.getPatientListDTOS(facilityId, stateOfResidence, lgaOfResidence));
    }
    
    
    @GetMapping("/get/{id}")
    public CaseManagerDTO GetCaseManagerById(@PathVariable int id){
        return caseManagerService.FindById(id);
    }

    @DeleteMapping("/delete/{id}")
    public String DeleteCaseManager(@PathVariable int id) throws Exception {
        return caseManagerService.Delete(id);
    }

    @GetMapping("/list")
    public List<CaseManagerDTO> GetAllCaseManager(){
            return caseManagerService.FindAll();
    }
}
