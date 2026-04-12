package com.problemfighter.pfspring.restapi.rr;

import com.problemfighter.pfspring.restapi.rr.response.DetailsResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ResponseProcessorNestedMappingTest {

    @Test
    void response_should_map_children_inside_parent_for_collection_subtypes() {
        ParentEntity entity = new ParentEntity();
        entity.name = "ambulance";

        ChildEntity childEntity = new ChildEntity();
        childEntity.code = "vehicle-1";
        entity.children.add(childEntity);

        DetailsResponse<ParentDto> response = new ResponseProcessor().response(entity, ParentDto.class);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.data);
        Assertions.assertEquals("ambulance", response.data.name);
        Assertions.assertNotNull(response.data.children);
        Assertions.assertEquals(1, response.data.children.size());
        Assertions.assertEquals("vehicle-1", response.data.children.get(0).code);
    }

    static class ParentEntity {
        String name;
        ProxyChildList children = new ProxyChildList();
    }

    static class ParentDto {
        String name;
        List<ChildDto> children;
    }

    static class ChildEntity {
        String code;
    }

    static class ChildDto {
        String code;
    }

    static class ProxyChildList extends ArrayList<ChildEntity> {
    }
}

