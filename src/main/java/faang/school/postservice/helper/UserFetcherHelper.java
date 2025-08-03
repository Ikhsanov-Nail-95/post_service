package faang.school.postservice.helper;

import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.service.UserServiceGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserFetcherHelper {

    private final UserServiceGateway userServiceGateway;

    @Value("${like.user-fetch.batch-size}")
    private int batchSize;

    public List<UserDto> fetchUsersInBatches(List<Long> userIds) {
        if (userIds.isEmpty()) {
            log.debug("No userIds to fetch — skipping call to user_service.");
            return List.of();
        }

        int total = userIds.size();
        int batches = (total + batchSize - 1) / batchSize;

        log.info("Sending {} batches to user_service ({} userIds total.)", batches, total);

        List<UserDto> result = new ArrayList<>();

        for (int i = 0; i < userIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, userIds.size());
            List<Long> batch = userIds.subList(i, end);
            result.addAll(userServiceGateway.getUsersByIds(batch));
        }

        return result;
    }
}