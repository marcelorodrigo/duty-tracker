package com.github.marcelorodrigo.dutytracker.gateway.controllers.profile;

import com.github.marcelorodrigo.dutytracker.gateway.api.ProfileManagementApi;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.ResourceLocation;
import com.github.marcelorodrigo.dutytracker.usecase.profile.CreateEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.DeleteEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.GetEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.UpdateEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.GetEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.UpdateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProfileController implements ProfileManagementApi {
    private final CreateEngineerProfileUseCase createProfile;
    private final GetEngineerProfileUseCase getProfile;
    private final UpdateEngineerProfileUseCase updateProfile;
    private final DeleteEngineerProfileUseCase deleteProfile;

    @Override
    public ResponseEntity<EngineerProfileResponse> createProfile(
            CreateEngineerProfileRequest createEngineerProfileRequest) {
        var response = createProfile.execute(createEngineerProfileRequest);
        log.atInfo().addKeyValue("profileId", response.id()).log("Engineer profile created");
        return ResponseEntity.created(ResourceLocation.fromCurrentRequest()).body(response);
    }

    @Override
    public ResponseEntity<EngineerProfileResponse> getProfile() {
        var response = getProfile.execute(new GetEngineerProfileRequest());
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<EngineerProfileResponse> updateProfile(
            UpdateEngineerProfileRequest updateEngineerProfileRequest) {
        var response = updateProfile.execute(updateEngineerProfileRequest);
        log.atInfo().addKeyValue("profileId", response.id()).log("Engineer profile updated");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteProfile() {
        log.atInfo().log("Engineer profile deleted");
        deleteProfile.execute(new DeleteEngineerProfileRequest());
        return ResponseEntity.noContent().build();
    }
}
