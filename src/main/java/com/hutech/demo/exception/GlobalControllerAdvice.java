package com.hutech.demo.exception;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.enums.CompanyStatus;
import com.hutech.demo.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CompanyService companyService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        try {
            List<Company> allCompanies = companyService.getAllCompanies();
            long pendingCount = allCompanies.stream()
                    .filter(c -> c.getStatus() == CompanyStatus.PENDING)
                    .count();
            model.addAttribute("pendingCompaniesCount", pendingCount);
        } catch (Exception e) {
            model.addAttribute("pendingCompaniesCount", 0L);
        }
    }
}
