package net.consensys.rlpapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RLPControllerTest {

  private RLPController controller = new RLPController();

  @Test
  void testDog() throws Exception {
    assertEquals("83646F67", controller.encode("dog"));
  }

  @Test
  void testDogCatList() throws Exception {
    assertEquals("C88363617483646F67", controller.encode("[\"cat\",\"dog\"]"));
  }

  @Test
  void testDecodeDog() throws Exception {
    assertEquals("\"dog\"", controller.decode("83646F67"));
  }

  @Test
  void testDecodeDogCatList() throws Exception {
    assertEquals("[\"cat\",\"dog\"]", controller.decode("C88363617483646F67"));
  }

}
