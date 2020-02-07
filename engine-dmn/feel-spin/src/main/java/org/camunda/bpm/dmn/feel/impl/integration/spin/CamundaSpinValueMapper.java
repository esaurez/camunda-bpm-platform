/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.dmn.feel.impl.integration.spin;

import static scala.jdk.CollectionConverters.ListHasAsScala;
import static scala.jdk.CollectionConverters.MapHasAsScala;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.camunda.feel.impl.spi.JavaCustomValueMapper;
import org.camunda.feel.interpreter.impl.Context.StaticContext;
import org.camunda.feel.interpreter.impl.Val;
import org.camunda.feel.interpreter.impl.ValContext;
import org.camunda.feel.interpreter.impl.ValFunction;
import org.camunda.feel.interpreter.impl.ValList;
import org.camunda.feel.interpreter.impl.ValNull$;
import org.camunda.feel.interpreter.impl.ValString;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlElement;
import org.camunda.spin.xml.SpinXmlNode;
import scala.Tuple2;
import scala.collection.immutable.List;
import scala.collection.mutable.Buffer;


public class CamundaSpinValueMapper extends JavaCustomValueMapper {

  @Override
  public Optional<Val> toValue(Object x, Function<Object, Val> innerValueMapper) {
    if (x instanceof SpinJsonNode) {
      SpinJsonNode node = (SpinJsonNode) x;
      return Optional.of(this.spinJsonToVal(node, innerValueMapper));

    } else if (x instanceof SpinXmlElement) {
      SpinXmlElement element = (SpinXmlElement) x;
      return Optional.of(this.spinXmlToVal(element));

    } else {
      return Optional.empty();

    }
  }

  @Override
  public Optional<Object> unpackValue(Val value, Function<Val, Object> innerValueMapper) {
    return Optional.empty();
  }

  @Override
  public int priority() {
    return 30;
  }

  protected Val spinJsonToVal(SpinJsonNode node, Function<Object, Val> innerValueMapper) {
    if (node.isObject()) {
       Buffer<String> fields = ListHasAsScala(node.fieldNames()).asScala();
       Buffer pairs = (Buffer) fields.map(field -> this.spinJsonToVal(node.prop(field), innerValueMapper));
       return new ValContext(new StaticContext(pairs.toMap()));

    } else if (node.isArray()) {
       Buffer<SpinJsonNode> elements = ListHasAsScala(node.elements()).asScala();
       List values = elements.map(e -> this.spinJsonToVal(e, innerValueMapper)).toList();
       return new ValList(values);

    } else if (node.isNull()) {
       return new ValNull$();
    } else {
      return innerValueMapper.apply(node.value());

    }
  }

  private Val spinXmlToVal(SpinXmlElement element) {
     String name = this.nodeName(element);
     Val value = this.spinXmlElementToVal(element);
     Map<String, Object> map = Collections.singletonMap(name, value);
     Map<String, List<ValFunction>> funcMap = Collections.EMPTY_MAP;

     return new ValContext(new StaticContext(MapHasAsScala(map).asScala(), MapHasAsScala(funcMap).asScala()));
  }

  private Val spinXmlElementToVal(final SpinXmlElement e) {
    java.util.Map<String, Val> membersMap = new HashMap();

    String content = e.textContent().trim();
    if (!content.isEmpty()) {
      membersMap.put("$content", new ValString(content));
    }

    java.util.Map<String, ValString> attributes = e.attrs().stream().collect(Collectors.toMap(attr -> spinXmlAttributeToKey(attr), attr -> new ValString(attr.value())));
    membersMap.putAll(attributes);

    e.childElements().stream().collect(Collectors.toMap(el -> nodeName(el), el -> spinXmlElementToVal(el))).

    if (membersMap.isEmpty()) {
      return new ValNull$();
    } else {
      return new ValContext(new StaticContext(MapHasAsScala(membersMap).asScala()));
    }
  }

  protected String spinXmlAttributeToKey(SpinXmlAttribute attribute) {
    return "@" + nodeName(attribute);
  }

  protected String nodeName(SpinXmlNode n) {
    return Optional.of(n.prefix())
                   .map(p -> p + "$" + n.name())
                   .orElse(n.name());
  }
}

