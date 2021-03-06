// Copyright 2015 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.packages;

import static com.google.common.truth.Truth.assertThat;

import com.google.devtools.build.lib.testutil.TestRuleClassProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link AttributeContainer}.
 */
@RunWith(JUnit4.class)
public class AttributeContainerTest {

  private RuleClass ruleClass;
  private AttributeContainer container;
  private Attribute attribute1;
  private Attribute attribute2;

  @Before
  public final void createAttributeContainer() throws Exception  {
    ruleClass =
        TestRuleClassProvider.getRuleClassProvider().getRuleClassMap().get("testing_dummy_rule");
    attribute1 = ruleClass.getAttributeByName("srcs");
    attribute2 = ruleClass.getAttributeByName("dummyinteger");
    container = new AttributeContainer(ruleClass);
  }

  @Test
  public void testAttributeSettingAndRetrieval() throws Exception {
    Object someValue1 = new Object();
    Object someValue2 = new Object();
    container.setAttributeValue(attribute1, someValue1, /*explicit=*/ true);
    container.setAttributeValue(attribute2, someValue2, /*explicit=*/ true);
    assertThat(container.getAttr(attribute1.getName())).isEqualTo(someValue1);
    assertThat(container.getAttr(attribute2.getName())).isEqualTo(someValue2);
    assertThat(container.getAttr("nomatch")).isNull();
  }

  @Test
  public void testExplicitSpecificationsByInstance() throws Exception {
    Object someValue = new Object();
    container.setAttributeValue(attribute1, someValue, true);
    container.setAttributeValue(attribute2, someValue, false);
    assertThat(container.isAttributeValueExplicitlySpecified(attribute1)).isTrue();
    assertThat(container.isAttributeValueExplicitlySpecified(attribute2)).isFalse();
  }

  @Test
  public void testPackedState() throws Exception {
    Random rng = new Random();
    // The state packing machinery has special behavior at multiples of 8,
    // so set enough explicit values to exercise that.
    final int numAttributes = 17;
    Attribute[] attributes = new Attribute[numAttributes];
    for (int attributeIndex = 0; attributeIndex < numAttributes; ++attributeIndex) {
      attributes[attributeIndex] = ruleClass.getAttribute(attributeIndex);
    }

    Object someValue = new Object();
    for (int explicitCount = 0; explicitCount <= numAttributes; ++explicitCount) {
        AttributeContainer container = new AttributeContainer(ruleClass);
        // Shuffle the attributes each time through, to exercise
        // different stored indices and orderings.
        Collections.shuffle(Arrays.asList(attributes));
        // Also randomly interleave calls to the two setters.
        int valuePassKey = rng.nextInt(1 << numAttributes);
        for (int pass = 0; pass <= 1; ++pass) {
          for (int i = 0; i < explicitCount; ++i) {
            if (pass == ((valuePassKey >> i) & 1)) {
              container.setAttributeValue(attributes[i], someValue, true);
            }
          }
        }

        for (int i = 0; i < numAttributes; ++i) {
          boolean expected = i < explicitCount;
          assertThat(container.isAttributeValueExplicitlySpecified(attributes[i]))
              .isEqualTo(expected);
        }
    }
  }
}
