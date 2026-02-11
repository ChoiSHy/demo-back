package com.example.common.converter;

import com.example.common.enums.YesNo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA에서 YesNo enum을 DB의 Y/N 문자로 변환
 */
@Converter(autoApply = true)
public class YesNoConverter implements AttributeConverter<YesNo, String> {

    @Override
    public String convertToDatabaseColumn(YesNo attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public YesNo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return YesNo.fromValue(dbData);
    }
}
