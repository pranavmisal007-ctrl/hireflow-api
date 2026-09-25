package com.hireflow.service;

import com.hireflow.entity.Job;
import com.hireflow.entity.SavedJob;
import com.hireflow.entity.User;
import com.hireflow.exception.DuplicateResourceException;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.exception.UnauthorizedException;
import com.hireflow.repository.JobRepository;
import com.hireflow.repository.SavedJobRepository;
import com.hireflow.repository.UserRepository;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveJob(UserPrincipal principal, Long jobId) {
        User seeker = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (savedJobRepository.existsBySeekerAndJobId(seeker, jobId)) {
            throw new DuplicateResourceException("SavedJob", "jobId", jobId);
        }

        SavedJob savedJob = SavedJob.builder()
            .seeker(seeker)
            .job(job)
            .savedAt(LocalDateTime.now())
            .build();
        savedJobRepository.save(savedJob);
    }

    @Transactional
    public void unsaveJob(UserPrincipal principal, Long jobId) {
        User seeker = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        savedJobRepository.deleteBySeekerAndJobId(seeker, jobId);
    }

    @Transactional(readOnly = true)
    public Page<SavedJob> getSavedJobs(UserPrincipal principal, int page, int size) {
        User seeker = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        return savedJobRepository.findBySeeker(seeker, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long countSavedJobs(UserPrincipal principal) {
        User seeker = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        return savedJobRepository.findBySeeker(seeker, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
    }
}
