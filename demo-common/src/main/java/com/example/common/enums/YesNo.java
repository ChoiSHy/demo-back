package com.example.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesNo {

    YES("Y"),
    NO("N");

    private final String value;

    /**
     * JSON 직렬화 시 value 값 반환
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * JSON 역직렬화 시 value로 enum 변환
     */
    @JsonCreator
    public static YesNo fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (YesNo yesNo : YesNo.values()) {
            if (yesNo.value.equalsIgnoreCase(value)) {
                return yesNo;
            }
        }
        throw new IllegalArgumentException("Unknown YesNo value: " + value);
    }

    /**
     * boolean 값으로 변환
     */
    public boolean toBoolean() {
        return this == YES;
    }

    /**
     * boolean 값에서 YesNo로 변환
     */
    public static YesNo fromBoolean(boolean value) {
        return value ? YES : NO;
    }
}
