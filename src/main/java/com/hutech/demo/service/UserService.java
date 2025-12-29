package com.hutech.demo.service;

import com.hutech.demo.dto.request.UpgradeToEmployerRequest;
import com.hutech.demo.model.Company;
import com.hutech.demo.model.User;
import com.hutech.demo.model.enums.CompanyStatus;
import com.hutech.demo.model.enums.UserRole;
import com.hutech.demo.repository.CompanyRepository;
import com.hutech.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setStatus(userDetails.getStatus());
        
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User upgradeToEmployer(Long userId, UpgradeToEmployerRequest request) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is already employer
        if (user.getRole() == UserRole.EMPLOYER || user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Bạn đã là nhà tuyển dụng hoặc quản trị viên");
        }
        
        // Check if user already has pending company request
        List<Company> existingRequests = companyRepository.findAll().stream()
                .filter(c -> c.getUser() != null && c.getUser().getId().equals(userId))
                .filter(c -> c.getStatus() == CompanyStatus.PENDING)
                .toList();
        
        if (!existingRequests.isEmpty()) {
            throw new RuntimeException("Bạn đã có yêu cầu nâng cấp đang chờ duyệt");
        }
        
        // Create company with PENDING status
        Company company = new Company();
        company.setCompanyName(request.getCompanyName());
        company.setAddress(request.getAddress());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setLogo(request.getLogo());
        company.setStatus(CompanyStatus.PENDING);
        company.setUser(user);
        
        // Debug: Check logo before saving
        System.out.println("=== Before Save ===");
        System.out.println("Company Logo: " + (company.getLogo() != null ? "Set (length: " + company.getLogo().length() + ")" : "NULL"));
        
        Company savedCompany = companyRepository.save(company);
        
        // Debug: Check logo after saving
        System.out.println("=== After Save ===");
        System.out.println("Saved Company ID: " + savedCompany.getId());
        System.out.println("Saved Company Logo: " + (savedCompany.getLogo() != null ? "Set (length: " + savedCompany.getLogo().length() + ")" : "NULL"));
        System.out.println("==================");
        
        // NOTE: User role will be upgraded to EMPLOYER when admin approves the company
        return user;
    }
}
