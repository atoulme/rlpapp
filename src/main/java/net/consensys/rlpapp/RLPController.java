package net.consensys.rlpapp;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.rlp.RLP;
import net.consensys.cava.rlp.RLPReader;
import net.consensys.cava.rlp.RLPWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RLPController {

  @RequestMapping(path = "/encode" , method = RequestMethod.POST , consumes = "application/json")
  public String encode(@RequestBody String json) throws IOException {
    if (!json.matches("\\s*\\[.*") && !json.startsWith("\"")) {
      json = "\"" + json + "\"";
    }
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(json);
    if (node.isArray()) {
      return RLP.encodeList(writer -> node.forEach(elt -> writeRLP(writer, elt))).toHexString().substring(2);
    } else if (node.isObject()) {
      throw new IllegalArgumentException("Cannot encode objects");
    } else {
      return RLP.encode(writer -> writeRLP(writer, node)).toHexString().substring(2);
    }
  }

  private void writeRLP(RLPWriter writer, JsonNode node) {
    if (node.isArray()) {
      writer.writeList(listWriter -> node.forEach(elt -> writeRLP(listWriter, elt)));
    } else if (node.isObject()) {
      throw new IllegalArgumentException("Cannot encode objects");
    } else {
      writer.writeString(((ValueNode) node).textValue());
    }
  }

  @RequestMapping(path = "/decode" , method = RequestMethod.POST , produces = "application/json")
  public String decode(@RequestBody String rlp) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    Bytes source = Bytes.fromHexString(rlp);

    if (RLP.isList(source)) {
      return mapper.writeValueAsString(RLP.decodeList(source, reader -> readRLP(reader, true)));
    } else {
      return mapper.writeValueAsString(RLP.decode(source, reader -> readRLP(reader, false)));
    }
  }

  private Object readRLP(RLPReader reader, boolean isList) {
    List<Object> elts = new ArrayList<>();
    while (!reader.isComplete()) {
      Object elt = null;
      if (reader.nextIsList()) {
        elt = reader.readList(listReader -> readRLP(listReader, true));
      } else {
        elt = new String(reader.readValue().toArrayUnsafe(), StandardCharsets.UTF_8);
      }
      if (isList) {
        elts.add(elt);
      } else {
        return elt;
      }
    }
    return elts;
  }

}
