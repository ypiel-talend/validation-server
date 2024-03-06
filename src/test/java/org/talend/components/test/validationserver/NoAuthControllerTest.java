package org.talend.components.test.validationserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;


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
    public void loadFile() throws IOException {
        URL resourceUrl = NoAuthControllerTest.class.getResource("/");
        File f = new File(resourceUrl.getPath());
        String file = f.getAbsolutePath() + "/loadFile/Simple.txt";
        String content = controller.loadFile(file);
        Assertions.assertEquals("This\nis\nan hellow world!", content);
    }

}