package com.dutytracker.usecase.profile;




import com.dutytracker.domain.EngineerProfile;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.stereotype.Service;
@Service
public class CreateEngineerProfileUseCase implements UseCase<CreateEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final CreateEngineerProfileValidator validator;

    public CreateEngineerProfileUseCase(EngineerProfileGateway profileGateway,
                                        CreateEngineerProfileValidator validator) {
        this.profileGateway = profileGateway;
        this.validator = validator;
    }

    @Override
    public EngineerProfileResponse execute(CreateEngineerProfileRequest request) {
        validator.validate(request);
        EngineerProfile profile = new EngineerProfile(
                null,
                request.employeeType(),
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                null
        );
        EngineerProfile saved = profileGateway.save(profile);
        List<String> days = saved.workingDays().stream().map(DayOfWeek::name).sorted().toList();
        return new EngineerProfileResponse(
                saved.id(),
                saved.employeeType(),
                days,
                saved.workStartTime(),
                saved.workEndTime(),
                false
        );
    }
}
