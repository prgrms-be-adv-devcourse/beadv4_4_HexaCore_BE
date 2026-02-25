package com.back.product.global.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.jsontype.impl.DefaultTypeResolverBuilder;

public class RecordSupportingTypeResolver extends DefaultTypeResolverBuilder {

    public RecordSupportingTypeResolver(PolymorphicTypeValidator typeValidator, DefaultTyping t) {
        super(typeValidator, t, JsonTypeInfo.As.PROPERTY);
    }

    @Override
    public boolean useForType(JavaType t) {
        // 배열(Array/Collection)이나 참조타입(Optional 등)으로 감싸진 경우 실제 알맹이 타입을 꺼냄
        JavaType unwrapped = _unwrapArrayType(t);
        unwrapped = _unwrapReferenceType(unwrapped);

        Class<?> rawClass = unwrapped.getRawClass();

        // 그 외의 모든 객체(Record, 불변 List, DTO 등)는 무조건 @class 강제 추가
        if (rawClass.isPrimitive() ||
                String.class.isAssignableFrom(rawClass) ||
                Number.class.isAssignableFrom(rawClass) ||
                Boolean.class.isAssignableFrom(rawClass) ||
                Enum.class.isAssignableFrom(rawClass) ||
                TreeNode.class.isAssignableFrom(rawClass)) {
            return false;
        }

        // 그 외의 모든 객체(Record, 불변 List, DTO 등)는 무조건 @class 강제 추가
        return true;
    }
}
