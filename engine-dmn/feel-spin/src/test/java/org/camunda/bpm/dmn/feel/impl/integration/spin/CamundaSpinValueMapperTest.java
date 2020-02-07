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

import java.util.Arrays;
import java.util.List;

import org.camunda.feel.impl.spi.CustomValueMapper;
import org.camunda.feel.interpreter.impl.Context;
import org.camunda.feel.interpreter.impl.DefaultValueMapper;
import org.camunda.feel.interpreter.impl.Val;
import org.camunda.feel.interpreter.impl.ValContext;
import org.camunda.feel.interpreter.impl.ValList;
import org.camunda.feel.interpreter.impl.ValString;
import org.camunda.feel.interpreter.impl.ValueMapper;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static scala.jdk.CollectionConverters.ListHasAsScala;

public class CamundaSpinValueMapperTest {

  protected static ValueMapper valueMapper;

  @BeforeClass
  public static void setUp() {
    List<CustomValueMapper> mapperList = Arrays.asList(DefaultValueMapper.instance(), new CamundaSpinValueMapper());
    valueMapper = new ValueMapper.CompositeValueMapper(ListHasAsScala(mapperList).asScala().toList());
  }

  @Test
  public void shouldMapCamundaSpinJSONobjectAsContext() {
    // given
    scala.collection.immutable.Map map = new scala.collection.immutable.Map.Map2("customer", new ValString("Kermit"), "language", new ValString("en"));
    ValContext context = new ValContext(new Context.StaticContext(map, null));
    SpinJsonNode json = Spin.JSON("{\"customer\": \"Kermit\", \"language\": \"en\"}");

    // when
    Val value = valueMapper.toVal(json);

    // then
    assertThat(value).isEqualTo(context);
  }

//  @Test
//  public void shouldMapCamundaSpinJSONarrayAsList() {
//    // given
//    scala.collection.immutable.Map map = new scala.collection.immutable.Map.Map2("customer", new ValList(new scala.collection.immutable.List), "language", new ValString("en"));
//    ValContext context = new ValContext(new Context.StaticContext(map, null));
//    SpinJsonNode json = Spin.JSON("{\"customer\": [\"Kermit\", \"Waldo\"]}");
//
//    // when
//    Val value = valueMapper.toVal(json);
//
//    // then
//    should be(
//      ValContext(
//        Context.StaticContext(
//          Map(
//            "customer" -> ValList(List(
//      ValString("Kermit"),
//      ValString("Waldo")
//                                      ))
//          )))
//    )
//  }
//
//  @Test
//  public void shouldMapNestedCamundaSpinJSONobjectAsContext() {
//    val json: SpinJsonNode = Spin.JSON(
//      """{"customer": "Kermit", "address": {"city": "Berlin", "zipCode": 10961}}""")
//
//    valueMapper.toVal(json) should be(
//      ValContext(
//        Context.StaticContext(
//          Map(
//            "customer" -> ValString("Kermit"),
//      "address" -> ValContext(
//      Context.StaticContext(
//        Map(
//          "city" -> ValString("Berlin"),
//      "zipCode" -> ValNumber(10961)
//                )
//              )
//            )
//          )))
//    )
//  }
//
//  @Test
//  public void shouldMapCamundaSpinXMLobjectWithAttributes() {
//    val xml: SpinXmlElement = Spin.XML(
//      """
//      <customer name="Kermit" language="en" />
//    """)
//
//    valueMapper.toVal(xml) should be(
//      ValContext(
//        Context.StaticContext(
//          Map(
//            "customer" -> ValContext(
//      Context.StaticContext(
//        Map(
//          "@name" -> ValString("Kermit"),
//      "@language" -> ValString("en")
//                )
//              )
//            )
//          )))
//    )
//  }
//
//  @Test
//  public void shouldMapCamundaSpinXMLobjectWithChildObject() {
//    val xml: SpinXmlElement = Spin.XML(
//      """
//      <customer>
//        <address city="Berlin" zipCode="10961" />
//      </customer>
//    """)
//
//    valueMapper.toVal(xml) should be(
//      ValContext(Context.StaticContext(Map(
//        "customer" -> ValContext(Context.StaticContext(Map(
//      "address" -> ValContext(Context.StaticContext(Map(
//      "@city" -> ValString("Berlin"),
//      "@zipCode" -> ValString("10961")
//          )))
//        )))
//      )))
//    )
//  }
//
//  @Test
//  public void shouldMapCamundaSpinXMLobjectWithListOfChildObjects() {
//    val xml: SpinXmlElement = Spin.XML(
//      """
//      <data>
//        <customer name="Kermit" language="en" />
//        <customer name="John" language="de" />
//        <provider name="Foobar" />
//      </data>
//    """)
//
//    valueMapper.toVal(xml) should be(
//      ValContext(Context.StaticContext(Map(
//        "data" -> ValContext(Context.StaticContext(Map(
//      "customer" -> ValList(List(
//      ValContext(Context.StaticContext(Map(
//        "@name" -> ValString("Kermit"),
//      "@language" -> ValString("en")
//            ))),
//    ValContext(Context.StaticContext(Map(
//      "@name" -> ValString("John"),
//      "@language" -> ValString("de")
//            )))
//          )),
//    "provider" -> ValContext(Context.StaticContext(Map(
//      "@name" -> ValString("Foobar")
//          )))
//        )))
//      )))
//    )
//  }
//
//  @Test
//  public void shouldMapCamundaSpinXMLobjectWithContent() {
//    val xml: SpinXmlElement = Spin.XML(
//      """
//      <customer>Kermit</customer>
//    """)
//
//    valueMapper.toVal(xml) should be(
//      ValContext(Context.StaticContext(Map(
//        "customer" -> ValContext(Context.StaticContext(Map(
//      "$content" -> ValString("Kermit")
//        )))
//      )))
//    )
//  }
//
//  @Test
//  public void shouldMapCamundaSpinXMLobjectWithoutContent() {
//    val xml: SpinXmlElement = Spin.XML(
//      """
//      <customer />
//    """)
//
//    valueMapper.toVal(xml) should be(
//      ValContext(Context.StaticContext(Map(
//        "customer" -> ValNull
//      )))
//    )
//  }
//
//  @Test
//  public void shouldMapCamundaSpinXMLobjectWithPrefix() {
//    val xml: SpinXmlElement =
//      Spin.XML(
//        """
//      <data xmlns:p="http://www.example.org">
//        <p:customer p:name="Kermit" language="en" />
//      </data>
//    """)
//
//    valueMapper.toVal(xml) should be(
//      ValContext(Context.StaticContext(Map(
//        "data" -> ValContext(Context.StaticContext(Map(
//      "p$customer" -> ValContext(Context.StaticContext(Map(
//      "@p$name" -> ValString("Kermit"),
//      "@language" -> ValString("en")
//          ))),
//    "@xmlns$p" -> ValString("http://www.example.org")
//        )))
//      )))
//    )
//  }
}
