package com.ourmemories.OurMemoriesEduSmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ApplicationDTO {
    private Long id;
    private String type;
    private List<String> institutions;
    private String status;
    private LocalDateTime date;
    private List<String> documentsNeeded;
    private BigDecimal applicationFee;
    private String paymentMethod;
    private String paymentStatus; // From payment entity
    private String displayStatus;
    private String statusColor;

    public ApplicationDTO(Long id, String type, List<String> institutions,
                          String status, LocalDateTime date, List<String> documentsNeeded,
                          BigDecimal applicationFee, String paymentMethod,
                          String paymentStatus, String displayStatus, String statusColor) {
        this.id = id;
        this.type = type;
        this.institutions = institutions;
        this.status = status;
        this.date = date;
        this.documentsNeeded = documentsNeeded;
        this.applicationFee = applicationFee;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.displayStatus = displayStatus;
        this.statusColor = statusColor;
    }
}