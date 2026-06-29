package com.example.distribute_lovable_clone.intelligence_service.service.impl;

import com.example.distribute_lovable_clone.intelligence_service.client.AccountClient;
import com.example.distribute_lovable_clone.intelligence_service.entity.UsageLog;
import com.example.distribute_lovable_clone.intelligence_service.repository.UsageLogRepository;
import com.example.distribute_lovable_clone.intelligence_service.service.UsageService;
import com.example.distributelovableclone.commonlib.dto.PlanDto;
import com.example.distributelovableclone.commonlib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepo;
    private final AuthUtil authUtil;
    private final AccountClient accountClient;

    @Override
    public void recordTokenUsage(Long userId, int actualTokens) {
        LocalDate today = LocalDate.now();

        try{
            UsageLog todayLog = usageLogRepo.findByUserIdAndDate(userId,today)
                    .orElseGet(()-> createNewDailyUsageLog(userId,today));

            todayLog.setTokensUsed(todayLog.getTokensUsed()+actualTokens);
            usageLogRepo.save(todayLog);

        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void checkDailyTokensUsage() {
        Long userId = authUtil.getCurrentUserId();
        PlanDto plan = accountClient.getCurrentSubscribedPlanByUser();

        LocalDate today = LocalDate.now();
        UsageLog todayLog = usageLogRepo.findByUserIdAndDate(userId,today)
                .orElseGet(()-> createNewDailyUsageLog(userId,today));

        if(plan.unlimitedAi()) return;

        int currentUsage = todayLog.getTokensUsed();
        int limit = plan.maxTokensPerDay();

        if (currentUsage>=limit)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Daily limit reached, Upgrade or come back later");

    }

    UsageLog createNewDailyUsageLog(Long userId,LocalDate date){
        return usageLogRepo.save(UsageLog.builder()
                        .userId(userId)
                        .date(date)
                        .tokensUsed(0)
                .build());
    }
}
