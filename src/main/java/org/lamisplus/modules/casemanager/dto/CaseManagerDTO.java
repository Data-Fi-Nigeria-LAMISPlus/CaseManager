package org.lamisplus.modules.casemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lamisplus.modules.casemanager.domain.AssignedPatient;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseManagerDTO {
    private Integer id;
    private String designation;
    private String firstName;
    private String lastName;
    private String sex;
    private String phoneNumber;
    private String uuid;
    private Long facilityId;
    private LocalDateTime createDate;
    private String address;
    private LocalDateTime dateModified;
    private Boolean active;
    private Integer archived;
    private String createdBy;
    private String modifiedBy;
    private String religion;
    private String username;
    private String password;
    private String user_id;
    private List<AssignedPatient> patients;
}
