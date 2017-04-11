package pl.mrugames.commons.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
class ArgumentResolver {
    private final ObjectMapper mapper;

    ArgumentResolver(ObjectMapper mapper) {
        this.mapper = mapper;
    }
}
