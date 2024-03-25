package org.talend.components.test.validationserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;


class NoAuthControllerTest {

    private NoAuthController controller;

    @BeforeEach
    public void beforeEach() {
        controller = new NoAuthController();
    }

    @Test
    public void pingPongTest(){
        String result = controller.pingTextPlain();
        Assertions.assertEquals(NoAuthController.PONG, result);
    }

    @Test
    public void paginate() {
        List<NoAuthController.Element> paginate = controller.paginate(null, null, null);
        Assertions.assertEquals(NoAuthController.DEFAULT_PAGINATION_LIMIT, paginate.size());

        paginate = controller.paginate(0,5, 5);
        Assertions.assertEquals(5, paginate.size());

        paginate = controller.paginate(0,5, 50);
        Assertions.assertEquals(5, paginate.size());
        Assertions.assertEquals(1, paginate.get(0).getId());
        Assertions.assertEquals(5, paginate.get(paginate.size() - 1).getId());

        paginate = controller.paginate(5,7, 50);
        Assertions.assertEquals(7, paginate.size());
        Assertions.assertEquals(6, paginate.get(0).getId());
        Assertions.assertEquals(12, paginate.get(paginate.size() - 1).getId());

        paginate = controller.paginate(5,7, 10);
        Assertions.assertEquals(5, paginate.size());
        Assertions.assertEquals(6, paginate.get(0).getId());
        Assertions.assertEquals(10, paginate.get(paginate.size() - 1).getId());

        paginate = controller.paginate(50,7, 10);
        Assertions.assertEquals(0, paginate.size());

    }

}