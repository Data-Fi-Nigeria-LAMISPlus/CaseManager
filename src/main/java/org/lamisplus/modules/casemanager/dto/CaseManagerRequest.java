package org.lamisplus.modules.casemanager.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CaseManagerRequest {
    private String designation;
    private String firstName;
    private String lastName;
    private String sex;
    private String phoneNumber;
    private String address;
    private String created_by;
    private String modified_by;
    private LocalDateTime date_modified;
    private Integer archived;
    private Boolean active;
    private String religion;
    private String uuid;
    private Long facilityId;
    private String username;
    private String password;
    private String user_id;
}
