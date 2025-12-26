package com.hutech.demo.service;

import com.hutech.demo.model.Profile;
import com.hutech.demo.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;

    public Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUser_Id(userId).orElse(null);
    }

    @Transactional
    public Profile updateProfile(Profile profile) {
        Profile existingProfile = profileRepository.findByUser_Id(profile.getUserId())
                .orElse(new Profile());
        
        existingProfile.setUserId(profile.getUserId());
        
        if (profile.getFullName() != null) {
            existingProfile.setFullName(profile.getFullName());
        }
        if (profile.getPhone() != null) {
            existingProfile.setPhone(profile.getPhone());
        }
        if (profile.getAddress() != null) {
            existingProfile.setAddress(profile.getAddress());
        }
        if (profile.getBio() != null) {
            existingProfile.setBio(profile.getBio());
        }
        if (profile.getAvatar() != null) {
            existingProfile.setAvatar(profile.getAvatar());
        }
        if (profile.getDateOfBirth() != null) {
            existingProfile.setDateOfBirth(profile.getDateOfBirth());
        }
        if (profile.getGender() != null) {
            existingProfile.setGender(profile.getGender());
        }
        
        return profileRepository.save(existingProfile);
    }
}
