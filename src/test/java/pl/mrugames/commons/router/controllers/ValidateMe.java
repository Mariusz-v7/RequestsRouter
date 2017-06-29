package pl.mrugames.commons.router.controllers;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Service
@Validated
public class ValidateMe {
    public int validateMe(boolean dummyParam, @Min(0) int value) {
        return value;
    }

    public void v2(@Min(0) int x, @Max(0) int y) {

    }
}
